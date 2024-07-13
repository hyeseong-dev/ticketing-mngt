package com.mgnt.reservationservice.domain.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgnt.core.enums.ReservationStatus;
import com.mgnt.core.enums.SeatStatus;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.event.concert_service.InventoryReservationRequestEvent;
import com.mgnt.core.event.concert_service.InventoryReservationResponseEvent;
import com.mgnt.core.event.concert_service.SeatReservationResponseEvent;
import com.mgnt.core.event.concert_service.SeatStatusUpdatedEvent;
import com.mgnt.core.event.payment_service.PaymentCompletedEvent;
import com.mgnt.core.event.reservation_service.*;
import com.mgnt.core.exception.CustomException;
import com.mgnt.reservationservice.controller.dto.request.ReservationRequest;
import com.mgnt.reservationservice.controller.dto.request.ReserveRequest;
import com.mgnt.reservationservice.controller.dto.response.ReservationResponseDTO;
import com.mgnt.reservationservice.domain.entity.Reservation;
import com.mgnt.reservationservice.domain.repository.ReservationRedisRepository;
import com.mgnt.reservationservice.domain.repository.ReservationRepository;
import com.mgnt.reservationservice.domain.service.ReservationService;
import com.mgnt.reservationservice.domain.service.ReservationValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ReservationRepository reservationRepository;
    private final ReservationValidator reservationValidator;
    private final ReservationRedisRepository reservationRedisRepository;
    private final ObjectMapper objectMapper;


    public List<ReservationResponseDTO> getMyReservations(Long userId) {
        // 먼저 Redis에서 캐시된 데이터 조회
        List<ReservationResponseDTO> cachedReservations = reservationRedisRepository.getUserReservations(userId);

        if (cachedReservations != null && !cachedReservations.isEmpty()) {
            log.info("캐시에서 예약 정보 조회: userId={}, count={}", userId, cachedReservations.size());
            return cachedReservations;
        }

        // 캐시에 데이터가 없으면 DB에서 조회
        List<Reservation> reservations = reservationRepository.findAllByUserId(userId);
        log.info("DB에서 예약 정보 조회: userId={}, count={}", userId, reservations.size());

        List<ReservationResponseDTO> responseDTOs = reservations.stream()
                .map(ReservationResponseDTO::from)
                .collect(Collectors.toList());

        // 조회한 데이터를 Redis에 캐시
        reservationRedisRepository.saveUserReservations(userId, responseDTOs);

        return responseDTOs;
    }

    private ReservationResponseDTO convertToDTO(Reservation reservation) {
        return new ReservationResponseDTO(
                reservation.getReservationId(),
                reservation.getStatus(),
                reservation.getUserId(),
                reservation.getConcertId(),
                reservation.getConcertDateId(),
                reservation.getSeatId(),
                reservation.getPrice(),
                reservation.getReservedAt()
        );
    }

    @Transactional
    public void updateReservation(Long reservationId, ReservationStatus newStatus) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));

        reservation.updateStatus(newStatus);
        reservationRepository.save(reservation);

        // 캐시 업데이트
        Long userId = reservation.getUserId();
        List<ReservationResponseDTO> userReservations = reservationRedisRepository.getUserReservations(userId);

        if (userReservations != null) {
            List<ReservationResponseDTO> updatedReservations = userReservations.stream()
                    .map(dto -> dto.reservationId().equals(reservationId) ? convertToDTO(reservation) : dto)
                    .collect(Collectors.toList());

            reservationRedisRepository.saveUserReservations(userId, updatedReservations);
        }
    }


// ========================================================================= API 구분선

    public void initiateReservation(Long userId, ReserveRequest request) {
        ReservationRequestedEvent event = new ReservationRequestedEvent(
                request.concertDateId(),
                userId,
                request.concertId(),
                request.seatId()
        );
        kafkaTemplate.send("reservation-requests", event);
        log.info("Reservation request initiated for user: {}, concert: {}", userId, request.concertId());
    }

    @KafkaListener(topics = "seat-status-updates")
    @Transactional
    public void handleSeatStatusUpdate(SeatStatusUpdatedEvent event) {
        try {

            Reservation reservation = Reservation.builder()
                    .userId(event.userId())
                    .concertId(event.concertId())
                    .concertDateId(event.concertDateId())
                    .seatId(event.seatId())
                    .status(ReservationStatus.ING)
                    .price(event.price())
                    .reservedAt(ZonedDateTime.now())
                    .build();
            reservationRepository.save(reservation);

            kafkaTemplate.send("reservations-created", new ReservationCreatedEvent(
                    reservation.getReservationId(), reservation.getUserId(), reservation.getPrice()));

        } catch (Exception e) {
            log.error("Error handling seat status update", e);
            kafkaTemplate.send("reservation-failed", new ReservationFailedEvent(
                    null, event.concertDateId(), event.seatId()));
        }
    }

    @KafkaListener(topics = "payment-completed")
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        try {
            Reservation reservation = reservationRepository.findById(event.reservationId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND, null, Level.WARN));

            if (event.isSuccess()) {
                reservation.updateStatus(ReservationStatus.RESERVED);
                reservationRepository.save(reservation);
                kafkaTemplate.send("reservation-confirmed", new ReservationConfirmedEvent(
                        reservation.getReservationId(), reservation.getConcertDateId(),
                        reservation.getSeatId(), SeatStatus.DISABLE));
            } else {
                reservation.updateStatus(ReservationStatus.CANCEL);
                reservationRepository.save(reservation);
                kafkaTemplate.send("reservation-failed", new ReservationFailedEvent(
                        event.reservationId(), reservation.getConcertDateId(), reservation.getSeatId()));
            }
        } catch (Exception e) {
            log.error("Error handling payment completed", e);
            kafkaTemplate.send("reservation-failed", new ReservationFailedEvent(
                    event.reservationId(), null, null));
        }
    }

    //---------------------------------------------------------------------------예약 테스트 락
    @Override
    @Transactional
    public ReservationInventoryCreateResponseDTO createReservationWithoutPayment(Long userId, ReservationRequest request) {
        try {
            Reservation reservation = Reservation.builder()
                    .userId(userId)
                    .concertId(request.concertId())
                    .concertDateId(request.concertDateId())
                    .seatId(request.seatId())
                    .price(request.price())
                    .status(ReservationStatus.ING)
                    .reservedAt(ZonedDateTime.now())
                    .build();

            reservation = reservationRepository.save(reservation);

            kafkaTemplate.send("inventory-reservation-requests", new InventoryReservationRequestEvent(
                    reservation.getReservationId(),
                    request.concertId(),
                    request.concertDateId(),
                    true
            ));

            return new ReservationInventoryCreateResponseDTO(
                    reservation.getReservationId(),
                    reservation.getUserId(),
                    reservation.getConcertId(),
                    reservation.getConcertDateId(),
                    reservation.getSeatId(),
                    reservation.getCreatedAt()
            );
        } catch (Exception e) {
            throw new CustomException(ErrorCode.RESERVATION_FAILED, e.getMessage(), Level.ERROR);
        }
    }

    @KafkaListener(topics = "inventory-reservation-responses")
    @Transactional
    public void handleInventoryReservationResponse(InventoryReservationResponseEvent event) {
        try {
            log.debug("[{}] 메소드 Received message from Kafka: {}", "handleInventoryReservationResponse", event);
            Reservation reservation = reservationRepository.findByReservationId(event.reservationId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND, null, Level.INFO));

            if (event.isSuccess()) {
                reservation.updateStatus(ReservationStatus.RESERVED);
                // reservation.updatePrice(getPrice(event.concertId()));
            } else {
                reservation.updateStatus(ReservationStatus.CANCEL);
            }

            reservationRepository.save(reservation);

            // Redis 캐시 업데이트
            ReservationResponseDTO updatedDto = ReservationResponseDTO.from(reservation);
            reservationRedisRepository.saveReservation(reservation.getUserId(), reservation.getReservationId(), updatedDto);

            // 클라이언트에게 결과 통지 (예: WebSocket 또는 SSE를 통해)
            notifyClient(reservation.getUserId(), updatedDto);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.RESERVATION_FAILED, e.getMessage(), Level.ERROR);
        }
    }


    private void notifyClient(Long userId, ReservationResponseDTO reservationResponseDTO) {
        // WebSocket 또는 SSE를 통한 클라이언트 통지 로직
    }

//    public Reservation addReservation(ReserveRequest request) {
//        return reservationRepository.save(request.toEntity());
//    }

//    @Override
//    @Transactional
//    public void cancel(Long reservationId, CancelRequest request) {
//        Reservation reservation = reservationRepository.findByIdAndUserId(reservationId, request.userId());
//
//        // validator
//        reservationValidator.isNull(reservation);
//
//        Payment payment = paymentReader.findPaymentByReservation(reservation);
//        if (payment != null) {
//            // 결제 내역 존재하면 환불 처리
//            paymentService.cancel(payment.getPaymentId());
//        }
//        reservationRepository.delete(reservation);
//    }
//

}
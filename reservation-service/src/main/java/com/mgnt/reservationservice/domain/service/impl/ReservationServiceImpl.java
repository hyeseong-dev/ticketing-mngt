package com.mgnt.reservationservice.domain.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgnt.core.enums.ReservationStatus;
import com.mgnt.core.enums.SeatStatus;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.event.concert_service.SeatReservationResponseEvent;
import com.mgnt.core.event.concert_service.SeatStatusUpdatedEvent;
import com.mgnt.core.event.payment_service.PaymentCompletedEvent;
import com.mgnt.core.event.reservation_service.ReservationConfirmedEvent;
import com.mgnt.core.event.reservation_service.ReservationCreatedEvent;
import com.mgnt.core.event.reservation_service.ReservationFailedEvent;
import com.mgnt.core.event.reservation_service.ReservationRequestedEvent;
import com.mgnt.core.exception.CustomException;
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
    public ReservationResponseDTO createReservationWithoutPayment(Long userId, ReserveRequest request) {

        // 결제 없이 예약을 생성하는 로직을 여기에 구현
        Reservation reservation = Reservation.builder()
                .userId(userId)
                .concertId(request.concertId())
                .concertDateId(request.concertDateId())
                .seatId(request.seatId())
                .status(ReservationStatus.ING) // 바로 RESERVED 상태로 설정
                .reservedAt(ZonedDateTime.now())
                .build();

        reservation = reservationRepository.save(reservation);

        // 좌석 상태 업데이트 이벤트 발행
        kafkaTemplate.send("seat-reservation-requests", new SeatStatusUpdatedEvent(
                reservation.getReservationId(),
                userId,
                request.concertId(),
                request.concertDateId(),
                request.seatId(),
                reservation.getPrice()
        ));

        // 3. 임시 응답 반환
        return ReservationResponseDTO.from(reservation);
    }

    @KafkaListener(topics = "seat-reservation-responses")
    @Transactional
    public void handleSeatReservationResponse(SeatReservationResponseEvent event) {
        Reservation reservation = reservationRepository.findById(event.reservationId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND, null, Level.INFO));

        boolean isSuccess = false;
        BigDecimal price = null;

        if (event.isSuccess()) {
            reservation.updateStatus(ReservationStatus.RESERVED);
            reservation.updatePrice(event.price());
        } else {
            reservation.updateStatus(ReservationStatus.CANCEL);
        }

        reservationRepository.save(reservation);

        // Redis 캐시 업데이트
        ReservationResponseDTO updatedDto = ReservationResponseDTO.from(reservation);
        reservationRedisRepository.saveReservation(reservation.getUserId(), reservation.getReservationId(), updatedDto);

        // 클라이언트에게 결과 통지 (예: WebSocket 또는 SSE를 통해)
        notifyClient(reservation.getUserId(), updatedDto);
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
package com.mgnt.reservationservice.domain.service;

import com.mgnt.core.enums.ReservationStatus;
import com.mgnt.core.enums.SeatStatus;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.event.*;
import com.mgnt.core.exception.CustomException;
import com.mgnt.reservationservice.controller.dto.request.ReserveRequest;
import com.mgnt.reservationservice.domain.entity.Reservation;
import com.mgnt.reservationservice.domain.repository.ReservationRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ReservationRepository reservationRepository;
    private final ReservationValidator reservationValidator;

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
//    @Override
//    public List<GetMyReservationsResponse> getMyReservations(Long userId) {
//        List<GetReservationAndPaymentResDto> myReservations = reservationRepository.getMyReservations(userId);
//        return myReservations.stream().map(GetMyReservationsResponse::from).toList();
//    }
}

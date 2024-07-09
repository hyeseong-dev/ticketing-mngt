package com.mgnt.reservationservice.domain.service;

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

    // 모든 좌석에 대한 고정 가격
    private static final BigDecimal FIXED_PRICE = new BigDecimal("50000.00");

    public void initiateReservation(ReserveRequest request) {
        ReservationRequestedEvent event = new ReservationRequestedEvent(
                request.concertDateId(),
                request.userId(),
                request.concertId(),
                request.seatId()
        );
        kafkaTemplate.send("reservation-requests", event);
        log.info("Reservation request initiated for user: {}, concert: {}", request.userId(), request.concertId());
    }

    @KafkaListener(topics = "seat-status-updates")
    @Transactional
    public void handleSeatStatusUpdate(SeatStatusUpdatedEvent event) {
        if (event.isAvailable()) {
            Reservation reservation = reservationRepository.save(Reservation.builder()
                    .userId(event.userId())
                    .concertId(event.concertId())
                    .concertDateId(event.concertDateId())
                    .seatId(event.seatId())
                    .status(Reservation.Status.ING)
                    .price(FIXED_PRICE)
                    .reservedAt(ZonedDateTime.now())
                    .build());

            ReservationCreatedEvent createdEvent = new ReservationCreatedEvent(
                    reservation.getReservationId(), reservation.getUserId(), reservation.getPrice());
            kafkaTemplate.send("reservations-created", createdEvent);
        } else {
            kafkaTemplate.send("reservation-failed",
                    new ReservationFailedEvent(event.reservationId(), event.concertId(), event.seatId()));
        }
    }

    @KafkaListener(topics = "payment-completed")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        Reservation reservation = reservationRepository.findById(event.reservationId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND, null, Level.INFO));

        if (event.isSuccess()) {
            reservation.updateStatus(Reservation.Status.RESERVED);
            reservationRepository.save(reservation);
            kafkaTemplate.send("reservation-confirmed", new ReservationConfirmedEvent(reservation.getReservationId()));
            log.info("Reservation confirmed after successful payment: {}", reservation.getReservationId());
        } else {
            reservation.updateStatus(Reservation.Status.CANCEL);
            reservationRepository.save(reservation);
            kafkaTemplate.send("reservation-failed",
                    new ReservationFailedEvent(event.reservationId(), reservation.getConcertDateId(), reservation.getSeatId()));
            log.warn("Reservation failed due to payment failure: {}", reservation.getReservationId());
        }
    }

    public Reservation addReservation(ReserveRequest request) {
        return reservationRepository.save(request.toEntity());
    }

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

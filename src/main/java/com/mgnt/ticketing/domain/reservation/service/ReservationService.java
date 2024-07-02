package com.mgnt.ticketing.domain.reservation.service;

import com.mgnt.ticketing.base.exception.CustomException;
import com.mgnt.ticketing.controller.reservation.dto.request.CancelRequest;
import com.mgnt.ticketing.controller.reservation.dto.request.ReserveRequest;
import com.mgnt.ticketing.controller.reservation.dto.response.ReserveResponse;
import com.mgnt.ticketing.controller.user.dto.response.GetMyReservationsResponse;
import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.concert.service.ConcertReader;
import com.mgnt.ticketing.domain.concert.service.ConcertService;
import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.payment.service.PaymentReader;
import com.mgnt.ticketing.domain.payment.service.PaymentService;
import com.mgnt.ticketing.domain.payment.service.dto.CancelPaymentResultResDto;
import com.mgnt.ticketing.domain.reservation.ReservationExceptionEnum;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.reservation.event.ReservationOccupiedEvent;
import com.mgnt.ticketing.domain.reservation.repository.ReservationRepository;
import com.mgnt.ticketing.domain.reservation.service.dto.GetReservationAndPaymentResDto;
import com.mgnt.ticketing.domain.user.service.UserReader;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService implements ReservationInterface {

    private final ReservationRepository reservationRepository;
    private final ReservationValidator reservationValidator;
    private final ReservationMonitor reservationMonitor;
    private final ConcertReader concertReader;
    private final ConcertService concertService;
    private final PaymentService paymentService;
    private final PaymentReader paymentReader;
    private final ApplicationEventPublisher eventPublisher;

    @PostConstruct
    public void init() {
        reservationMonitor.reservationMonitoring();
    }

    @Override
    @Transactional
    public ReserveResponse reserve(ReserveRequest request) {
        try {
            // 동시성 제어 - 비관적 락 적용
            concertService.patchSeatStatus(request.concertDateId(), request.seatNum(), Seat.Status.DISABLE);

            // validator
            reservationValidator.checkReserved(request.concertDateId(), request.seatNum());

            Reservation reservation = addReservation(request);

            Concert concert = concertReader.findConcert(reservation.getConcertId());
            ConcertDate concertDate = concertReader.findConcertDate(reservation.getConcertDateId());
            Seat seat = concertReader.findSeat(reservation.getConcertDateId(), reservation.getSeatNum());

            // 예약 임시 점유 event 발행
            eventPublisher.publishEvent(new ReservationOccupiedEvent(this, reservation.getReservationId()));

            return ReserveResponse.from(reservation, concert, concertDate, seat);

        } catch (PessimisticLockException e) {
            // 락 획득 실패 시
            throw new CustomException(ReservationExceptionEnum.ALREADY_RESERVED, null, LogLevel.INFO);
        }
    }

    public Reservation addReservation(ReserveRequest request) {
        return reservationRepository.save(request.toEntity());
    }

    @Override
    @Transactional
    public void cancel(Long reservationId, CancelRequest request) {
        Reservation reservation = reservationRepository.findByIdAndUserId(reservationId, request.userId());

        // validator
        reservationValidator.isNull(reservation);

        Payment payment = paymentReader.findPaymentByReservation(reservation);
        if (payment != null) {
            // 결제 내역 존재하면 환불 처리
            paymentService.cancel(payment.getPaymentId());
        }
        reservationRepository.delete(reservation);
    }

    @Override
    public List<GetMyReservationsResponse> getMyReservations(Long userId) {
        List<GetReservationAndPaymentResDto> myReservations = reservationRepository.getMyReservations(userId);
        return myReservations.stream().map(GetMyReservationsResponse::from).toList();
    }
}

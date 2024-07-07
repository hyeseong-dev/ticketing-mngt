package com.mgnt.reservationservice.domain.service;

import com.mgnt.core.exception.CustomException;
import com.mgnt.reservationservice.controller.dto.request.CancelRequest;
import com.mgnt.reservationservice.controller.dto.request.ReserveRequest;
import com.mgnt.reservationservice.controller.dto.response.ReserveResponse;
import com.mgnt.reservationservice.domain.ReservationExceptionEnum;
import com.mgnt.reservationservice.domain.entity.Reservation;
import com.mgnt.reservationservice.domain.event.ReservationOccupiedEvent;
import com.mgnt.reservationservice.domain.repository.ReservationRepository;
import com.mgnt.reservationservice.domain.service.dto.GetReservationAndPaymentResDto;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.ApplicationEventPublisher;
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

            Reservation reservation = reservationRepository.save(request.toEntity());

            Concert concert = concertReader.findConcert(reservation.getConcertId());
            ConcertDate concertDate = concertReader.findConcertDate(reservation.getConcertDateId());
            Seat seat = concertReader.findSeat(reservation.getConcertDateId(), reservation.getSeatNum());

            // 예약 임시 점유 event 발행
            eventPublisher.publishEvent(new ReservationOccupiedEvent(this, reservation.getReservationId()));

            return ReserveResponse.from(reservation, concert, concertDate, seat);

        } catch (ObjectOptimisticLockingFailureException e) {
            // 락 획득 실패 시
            throw new CustomException(ReservationExceptionEnum.ALREADY_RESERVED, null, LogLevel.INFO);
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

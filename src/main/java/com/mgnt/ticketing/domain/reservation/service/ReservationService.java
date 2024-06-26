package com.mgnt.ticketing.domain.reservation.service;

import com.mgnt.ticketing.controller.reservation.dto.request.CancelRequest;
import com.mgnt.ticketing.controller.reservation.dto.request.ReserveRequest;
import com.mgnt.ticketing.controller.reservation.dto.response.ReserveResponse;
import com.mgnt.ticketing.domain.concert.service.ConcertReader;
import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.payment.service.PaymentManager;
import com.mgnt.ticketing.domain.payment.service.dto.CancelPaymentResultResDto;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.reservation.repository.ReservationRepository;
import com.mgnt.ticketing.domain.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService implements ReservationInterface {

    private final ReservationRepository reservationRepository;
    private final ReservationValidator reservationValidator;
    private final ConcertReader concertReader;
    private final UserReader userReader;
    private final PaymentManager paymentManager;

    @Override
    public ReserveResponse reserve(ReserveRequest request) {
        // validator
        reservationValidator.checkReserved(request.concertDateId(), request.seatId());

        // 좌석 예약
        Reservation reservation = reservationRepository.save(request.toEntity(concertReader, userReader));
        // 결제 정보 생성
        Payment payment = paymentManager.create(reservation.toCreatePayment());
        // TODO - 5분 선점

        return ReserveResponse.from(reservation, payment);
    }

    @Override
    public void cancel(Long reservationId, CancelRequest request) {
        Reservation reservation = reservationRepository.findByIdAndUserId(reservationId, request.userId());

        // validator
        reservationValidator.isNull(reservation);

        // 취소
        // 1. 결제 정보 처리
        Payment payment = paymentManager.getPaymentByReservation(reservation);
        CancelPaymentResultResDto cancelPaymentInfo = paymentManager.cancel(payment);
        if (cancelPaymentInfo.isSuccess()) { // 결제 정보 처리 성공 시
            // 2. 예약 내역 삭제
            reservationRepository.delete(reservation);
        }
    }
}

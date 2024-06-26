package com.mgnt.ticketing.domain.reservation.service;

import com.mgnt.ticketing.controller.reservation.dto.request.CancelRequest;
import com.mgnt.ticketing.controller.reservation.dto.request.ReserveRequest;
import com.mgnt.ticketing.controller.reservation.dto.response.ReserveResponse;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;

import java.util.List;

public interface ReservationInterface {

    // 콘서트 좌석 예매
    ReserveResponse reserve(ReserveRequest request);

    // 좌석 예매 취소
    void cancel(Long reservationId, CancelRequest request);

    // 콘서트 회차별 예매정보 조회
    List<Reservation> getReservationsByConcertDate(Long concertDateId);
}
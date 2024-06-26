package com.mgnt.ticketing.domain.reservation.service;

import com.mgnt.ticketing.controller.reservation.dto.request.CancelRequest;
import com.mgnt.ticketing.controller.reservation.dto.request.ReserveRequest;
import com.mgnt.ticketing.controller.reservation.dto.response.ReserveResponse;

public interface ReservationInterface {

    // 콘서트 좌석 예매
    ReserveResponse reserve(ReserveRequest request);

    // 좌석 예매 취소
    void cancel(Long reservationId, CancelRequest request);

}
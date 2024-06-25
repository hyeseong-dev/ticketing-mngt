package com.mgnt.ticketing.domain.reservation.service;

import com.mgnt.ticketing.controller.reservation.dto.request.CancelRequest;
import com.mgnt.ticketing.controller.reservation.dto.request.ReserveRequest;
import com.mgnt.ticketing.controller.reservation.dto.response.ReserveResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationService implements ReservationInterface {

    @Override
    public ReserveResponse reserve(ReserveRequest request) {
        return null;
    }

    @Override
    public void cancel(Long reservationId, CancelRequest request) {

    }
}

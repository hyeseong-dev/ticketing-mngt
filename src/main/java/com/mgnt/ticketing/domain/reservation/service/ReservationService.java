package com.mgnt.ticketing.domain.reservation.service;

import com.mgnt.ticketing.controller.reservation.dto.request.CancelRequest;
import com.mgnt.ticketing.controller.reservation.dto.request.ReserveRequest;
import com.mgnt.ticketing.controller.reservation.dto.response.ReserveResponse;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService implements ReservationInterface {

    private final ReservationRepository reservationRepository;

    @Override
    public ReserveResponse reserve(ReserveRequest request) {
        return null;
    }
    @Override
    public void cancel(Long reservationId, CancelRequest request) {

    }

    @Override
    public List<Reservation> getReservationsByConcertDate(Long concertDateId) {
        return reservationRepository.findAllByConcertDateId(concertDateId);
    }
}
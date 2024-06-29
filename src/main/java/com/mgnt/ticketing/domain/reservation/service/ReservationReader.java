package com.mgnt.ticketing.domain.reservation.service;

import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationReader {
    /* Reservation 관련 정보 단순 조회용 */

    private final ReservationRepository reservationRepository;

    public Reservation findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId);
    }
}

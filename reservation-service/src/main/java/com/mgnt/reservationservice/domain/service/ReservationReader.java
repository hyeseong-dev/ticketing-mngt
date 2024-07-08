package com.mgnt.reservationservice.domain.service;

import com.mgnt.reservationservice.domain.entity.Reservation;
import com.mgnt.reservationservice.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationReader {
    /* Reservation 관련 정보 단순 조회용 */

    private final ReservationRepository reservationRepository;

    public Reservation findReservation(Long reservationId) {
        return reservationRepository.findByReservationId(reservationId);
    }
}

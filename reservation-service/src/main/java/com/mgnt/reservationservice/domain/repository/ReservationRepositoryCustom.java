package com.mgnt.reservationservice.domain.repository;

import com.mgnt.reservationservice.domain.entity.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepositoryCustom {
    List<Reservation> findAllByUserId(Long userId);

    Optional<Reservation> findByReservationId(Long reservationId);
}

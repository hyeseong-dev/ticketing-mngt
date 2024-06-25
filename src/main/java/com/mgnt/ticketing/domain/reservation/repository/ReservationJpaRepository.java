package com.mgnt.ticketing.domain.reservation.repository;

import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {
}

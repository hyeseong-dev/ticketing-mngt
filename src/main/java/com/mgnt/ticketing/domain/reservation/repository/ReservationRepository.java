package com.mgnt.ticketing.domain.reservation.repository;

import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository {

    List<Reservation> findAllByConcertDateId(Long concertDateId);
}
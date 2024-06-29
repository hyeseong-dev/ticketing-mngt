package com.mgnt.ticketing.domain.concert.repository;

import com.mgnt.ticketing.domain.concert.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatJpaRepository extends JpaRepository<Seat, Long> {
    boolean existsByConcertDate_concertDateIdAndStatus(Long concertDateId, Seat.Status status);

    List<Seat> findAllByConcertDate_concertDateIdAndStatus(Long concertDateId, Seat.Status status);
}


package com.mgnt.concertservice.domain.repository;

import com.mgnt.concertservice.domain.entity.Concert;
import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.core.enums.SeatStatus;

import java.util.List;
import java.util.Optional;

public interface ConcertRepositoryCustom {

    Optional<Concert> findByConcertId(Long concertId);

    List<Concert> findAllWithPlace();

    Optional<Concert> findByConcertIdWithPlace(Long concertId);

    List<Seat> findSeatsByConcertDateIdAndStatus(Long concertDateId, SeatStatus status);

    boolean existsByConcertDateAndStatus(Long concertDateId, SeatStatus status);

    Optional<Seat> findSeatByConcertDateIdAndSeatId(Long concertDateId, Long seatId);
}
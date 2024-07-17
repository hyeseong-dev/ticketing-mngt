package com.mgnt.concertservice.domain.repository;

import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.core.enums.SeatStatus;

import java.util.List;
import java.util.Optional;

public interface SeatRepositoryCustom {

    Optional<Seat> findByConsertDateIdAndSeatId(Long concertDateId, Long seatId);

    Optional<Seat> findAvailableSeatByConcertDateIdAndSeatId(Long concertDateId, Long seatId);

    int updateSeatStatus(Long concertDateId, Long seatId, SeatStatus seatStatus);

    List<Seat> findSeatsByConcertDateId(Long concertDateId);

    Optional<Seat> findAndLockByConcertDateIdAndSeatId(Long concertDateId, Long seatId);

    Optional<Seat> findSeatByConcertDateIdAndSeatId(Long concertDateId, Long seatId);

    boolean existsByConcertDateIdAndStatus(Long concertDateId, SeatStatus status);

    List<Seat> findAllByConcertDateIdAndStatus(Long concertDateId, SeatStatus status);

    List<Seat> findAllByConcertDateId(Long concertDateId);

    List<Seat> findAllAvailableSeatsByConcertDateIdAndStatus(Long concertDateId, SeatStatus status);
}

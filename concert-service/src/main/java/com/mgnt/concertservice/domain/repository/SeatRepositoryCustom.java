package com.mgnt.concertservice.domain.repository;

import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.core.enums.SeatStatus;

import java.util.Optional;

public interface SeatRepositoryCustom {

    Optional<Seat> findByConsertDateIdAndSeatId(Long concertDateId, Long seatId);

    Optional<Seat> findAvailableSeatByConcertDateIdAndSeatId(Long concertDateId, Long seatId);

    int updateSeatStatus(Long concertDateId, Long seatId, SeatStatus seatStatus);
}

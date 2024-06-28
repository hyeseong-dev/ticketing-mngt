package com.mgnt.ticketing.domain.concert.repository;

import com.mgnt.ticketing.domain.concert.dto.GetSeatsQueryResDto;
import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConcertRepository {
    List<Concert> findAll();

    Concert findById(Long concertId);

    ConcertDate findConcertDateById(Long concertDateId);

    void addConcertDates(List<ConcertDate> concertDates);

    void addConcert(Concert concert);

    void deleteAll();

    void deleteAllDates();

    boolean existByConcertDateAndStatus(Long concertDateId, Seat.Status status);

    List<Seat> findSeatsByConcertDateIdAndStatus(Long concertDateId, Seat.Status status);

    Seat findSeatById(Long seatId);

    void addSeats(List<Seat> seats);
}
package com.mgnt.ticketing.domain.concert.repository;

import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import org.springframework.data.repository.query.Param;
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

    boolean existByConcertDateAndStatus(Long concertDateId, SeatStatus status);

    List<Seat> findSeatsByConcertDateIdAndStatus(Long concertDateId, SeatStatus status);

    Seat findSeatByConcertDateIdAndSeatNum(@Param("concertDateId") Long concertDateId, @Param("seatNum") int seatNum);

    void addSeats(List<Seat> seats);
}

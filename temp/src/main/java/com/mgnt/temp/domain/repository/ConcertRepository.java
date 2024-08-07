package com.mgnt.temp.domain.repository;

import com.mgnt.temp.domain.entity.ConcertDate;
import com.mgnt.temp.domain.entity.Seat;
import com.mgnt.ticketing.domain.concert.entity.Concert;
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

    boolean existByConcertDateAndStatus(Long concertDateId, Seat.Status status);

    List<Seat> findSeatsByConcertDateIdAndStatus(Long concertDateId, Seat.Status status);

    Seat findSeatByConcertDateIdAndSeatNum(@Param("concertDateId") Long concertDateId, @Param("seatNum") int seatNum);

    void addSeats(List<Seat> seats);
}

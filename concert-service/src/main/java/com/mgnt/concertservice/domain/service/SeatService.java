package com.mgnt.concertservice.domain.service;

import com.mgnt.concertservice.domain.entity.Seat;

import java.util.List;

public interface SeatService {

    List<Seat> getAvailableSeats(Long concertDateId);

    List<Seat> getAllSeatsByConcertDateId(Long concertDateId);

}

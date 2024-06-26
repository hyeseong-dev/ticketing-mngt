package com.mgnt.ticketing.domain.concert.service;

import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import com.mgnt.ticketing.domain.concert.entity.Place;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.concert.repository.ConcertRepository;

import com.mgnt.ticketing.domain.concert.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConcertReader {
    private final ConcertRepository concertRepository;
    private final PlaceRepository placeRepository;

    public Concert getConcert(Long concertId) {
        return concertRepository.findById(concertId);
    }

    public ConcertDate getConcertDate(Long concertDateId) {
        return concertRepository.findConcertDateById(concertDateId);
    }

    public Place getPlace(Long placeId) {
        return placeRepository.findById(placeId);
    }

    public Seat getSeat(Long seatId) {
        return placeRepository.findSeatById(seatId);
    }
}

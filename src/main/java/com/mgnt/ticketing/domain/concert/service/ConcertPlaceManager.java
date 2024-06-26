package com.mgnt.ticketing.domain.concert.service;

import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.Place;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.concert.repository.ConcertRepository;
import com.mgnt.ticketing.domain.concert.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConcertPlaceManager {

    private final ConcertRepository concertRepository;
    private final PlaceRepository placeRepository;

    public List<Seat> getSeatsByPlace(Long concertId) {
        Concert concert = concertRepository.findById(concertId);
        return placeRepository.findById(concert.getPlaceId()).getSeatList();
    }

    public Place getPlace(Long placeId) {
        return placeRepository.findById(placeId);
    }
}

package com.mgnt.ticketing.domain.place.service;

import com.mgnt.ticketing.domain.place.entity.Place;
import com.mgnt.ticketing.domain.place.entity.Seat;
import com.mgnt.ticketing.domain.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceService implements PlaceInterface {

    private final PlaceRepository placeRepository;

    @Override
    public List<Seat> getSeatsByPlace(Long placeId) {
        return placeRepository.findById(placeId).getSeatList();
    }

    public Place getPlace(Long placeId) {
        return placeRepository.findById(placeId);
    }
}
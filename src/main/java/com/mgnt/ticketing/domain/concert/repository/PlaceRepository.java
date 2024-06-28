package com.mgnt.ticketing.domain.concert.repository;

import com.mgnt.ticketing.domain.concert.entity.Place;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository {

    Place findById(Long placeId);

    void addPlace(Place place);
}
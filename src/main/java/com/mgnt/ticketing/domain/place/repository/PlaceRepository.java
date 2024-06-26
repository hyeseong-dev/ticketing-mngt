package com.mgnt.ticketing.domain.place.repository;

import com.mgnt.ticketing.domain.place.entity.Place;

public interface PlaceRepository {

    Place findById(Long placeId);
}
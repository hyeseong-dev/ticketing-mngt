package com.mgnt.temp.domain.repository;

import com.mgnt.temp.domain.entity.Place;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository {

    Place findById(Long placeId);

    void addPlace(Place place);
}
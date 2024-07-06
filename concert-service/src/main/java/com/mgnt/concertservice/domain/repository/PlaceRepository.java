package com.mgnt.concertservice.domain.repository;

import com.mgnt.concertservice.domain.entity.Place;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository {

    Place findById(Long placeId);

    void addPlace(Place place);
}
package com.mgnt.ticketing.domain.concert.repository;

import com.mgnt.ticketing.domain.concert.entity.Place;
import org.springframework.stereotype.Repository;

public interface PlaceRepository {

    Place findById(Long placeId);
}
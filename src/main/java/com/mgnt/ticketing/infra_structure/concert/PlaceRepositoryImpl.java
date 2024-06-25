package com.mgnt.ticketing.infra_structure.concert;

import com.mgnt.ticketing.domain.concert.entity.Place;
import com.mgnt.ticketing.domain.concert.repository.PlaceJpaRepository;
import com.mgnt.ticketing.domain.concert.repository.PlaceRepository;

import java.util.NoSuchElementException;

public class PlaceRepositoryImpl implements PlaceRepository {

    private PlaceJpaRepository placeJpaRepository;

    @Override
    public Place findById(Long placeId) {
        return placeJpaRepository.findById(placeId).orElseThrow(NoSuchElementException::new);
    }
}
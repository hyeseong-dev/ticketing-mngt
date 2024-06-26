package com.mgnt.ticketing.infra_structure.place;

import com.mgnt.ticketing.domain.place.entity.Place;
import com.mgnt.ticketing.domain.place.repository.PlaceJpaRepository;
import com.mgnt.ticketing.domain.place.repository.PlaceRepository;
import org.springframework.stereotype.Repository;

import java.util.NoSuchElementException;

@Repository
public class PlaceRepositoryImpl implements PlaceRepository {

    private final PlaceJpaRepository placeJpaRepository;

    public PlaceRepositoryImpl(PlaceJpaRepository placeJpaRepository) {
        this.placeJpaRepository = placeJpaRepository;
    }

    @Override
    public Place findById(Long placeId) {
        return placeJpaRepository.findById(placeId).orElseThrow(NoSuchElementException::new);
    }
}
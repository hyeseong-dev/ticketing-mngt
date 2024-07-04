package com.mgnt.temp.infra_structure;

import com.mgnt.temp.domain.entity.Place;
import com.mgnt.temp.domain.repository.PlaceJpaRepository;
import com.mgnt.temp.domain.repository.PlaceRepository;
import com.mgnt.temp.domain.repository.SeatJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
public class PlaceRepositoryImpl implements PlaceRepository {

    private final PlaceJpaRepository placeJpaRepository;

    public PlaceRepositoryImpl(PlaceJpaRepository placeJpaRepository, SeatJpaRepository seatJpaRepository) {
        this.placeJpaRepository = placeJpaRepository;
    }

    @Override
    public Place findById(Long placeId) {
        return placeJpaRepository.findById(placeId).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public void addPlace(Place place) {
        placeJpaRepository.save(place);
    }
}

package com.mgnt.ticketing.infra_structure.concert;

import com.mgnt.ticketing.domain.concert.entity.Place;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.concert.repository.PlaceJpaRepository;
import com.mgnt.ticketing.domain.concert.repository.PlaceRepository;
import com.mgnt.ticketing.domain.concert.repository.SeatJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PlaceRepositoryImpl implements PlaceRepository {

    private final PlaceJpaRepository placeJpaRepository;
    private final SeatJpaRepository seatJpaRepository;

    public PlaceRepositoryImpl(PlaceJpaRepository placeJpaRepository, SeatJpaRepository seatJpaRepository) {
        this.placeJpaRepository = placeJpaRepository;
        this.seatJpaRepository = seatJpaRepository;
    }

    @Override
    public Place findById(Long placeId) {
        return placeJpaRepository.findById(placeId).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public Seat findSeatById(Long seatId) {
        return seatJpaRepository.findById(seatId).orElseThrow(EntityNotFoundException::new);
    }


    @Override
    public void addSeats(List<Seat> seats) {
        seatJpaRepository.saveAll(seats);
    }

    @Override
    public void addPlace(Place place) {
        placeJpaRepository.save(place);
    }
}

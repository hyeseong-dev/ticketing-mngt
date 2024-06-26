package com.mgnt.ticketing.domain.place.repository;

import com.mgnt.ticketing.domain.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceJpaRepository extends JpaRepository<Place, Long> {
}
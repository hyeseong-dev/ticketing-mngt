package com.mgnt.ticketing.domain.concert.repository;

import com.mgnt.ticketing.domain.concert.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceJpaRepository extends JpaRepository<Place, Long> {
}


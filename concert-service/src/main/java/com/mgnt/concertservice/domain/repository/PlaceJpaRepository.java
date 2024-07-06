package com.mgnt.concertservice.domain.repository;

import com.mgnt.concertservice.domain.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceJpaRepository extends JpaRepository<Place, Long> {
}


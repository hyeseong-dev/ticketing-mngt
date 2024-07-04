package com.mgnt.temp.domain.repository;

import com.mgnt.temp.domain.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceJpaRepository extends JpaRepository<Place, Long> {
}


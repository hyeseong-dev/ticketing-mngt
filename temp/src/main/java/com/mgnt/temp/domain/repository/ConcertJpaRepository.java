package com.mgnt.temp.domain.repository;

import com.mgnt.ticketing.domain.concert.entity.Concert;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ConcertJpaRepository extends JpaRepository<Concert, Long> {
}

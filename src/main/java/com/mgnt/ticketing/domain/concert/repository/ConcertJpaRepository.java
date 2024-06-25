package com.mgnt.ticketing.domain.concert.repository;

import com.mgnt.ticketing.domain.concert.entity.Concert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertJpaRepository extends JpaRepository<Concert, Long> {
}

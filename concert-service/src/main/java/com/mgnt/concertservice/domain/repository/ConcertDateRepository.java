package com.mgnt.concertservice.domain.repository;

import com.mgnt.concertservice.domain.entity.ConcertDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConcertDateRepository extends JpaRepository<ConcertDate, Long> {

    Optional<ConcertDate> findByConcertDateId(Long consertDateId);
}

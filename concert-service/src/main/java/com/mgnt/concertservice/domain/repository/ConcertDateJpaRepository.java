package com.mgnt.concertservice.domain.repository;

import com.mgnt.concertservice.domain.entity.ConcertDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertDateJpaRepository extends JpaRepository<ConcertDate, Long> {
}

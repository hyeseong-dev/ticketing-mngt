package com.mgnt.temp.domain.repository;

import com.mgnt.temp.domain.entity.ConcertDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertDateJpaRepository extends JpaRepository<ConcertDate, Long> {
}

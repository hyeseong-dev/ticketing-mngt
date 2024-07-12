package com.mgnt.concertservice.domain.repository;

import com.mgnt.concertservice.domain.entity.Concert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcertRepository extends JpaRepository<Concert, Long>, ConcertRepositoryCustom {
}

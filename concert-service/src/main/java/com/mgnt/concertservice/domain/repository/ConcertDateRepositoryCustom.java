package com.mgnt.concertservice.domain.repository;

import com.mgnt.concertservice.domain.entity.ConcertDate;

import java.util.Optional;

public interface ConcertDateRepositoryCustom {

    Optional<ConcertDate> findByConcertDateId(Long concertDateId);
}

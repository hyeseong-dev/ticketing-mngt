package com.mgnt.concertservice.domain.repository.impl;

import com.mgnt.concertservice.domain.entity.ConcertDate;
import com.mgnt.concertservice.domain.repository.ConcertDateRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ConcertDateRepositoryImpl implements ConcertDateRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<ConcertDate> findByConcertDateId(Long concertDateId) {
        return Optional.ofNullable(entityManager.find(ConcertDate.class, concertDateId));
    }
}

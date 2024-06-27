package com.mgnt.ticketing.infra_structure.concert;


import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import com.mgnt.ticketing.domain.concert.repository.ConcertDateJpaRepository;
import com.mgnt.ticketing.domain.concert.repository.ConcertJpaRepository;
import com.mgnt.ticketing.domain.concert.repository.ConcertRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ConcertRepositoryImpl implements ConcertRepository {

    private final ConcertJpaRepository concertJpaRepository;
    private final ConcertDateJpaRepository concertDateJpaRepository;

    public ConcertRepositoryImpl(ConcertJpaRepository concertJpaRepository, ConcertDateJpaRepository concertDateJpaRepository, ConcertDateJpaRepository concertDateJpaRepository1) {
        this.concertJpaRepository = concertJpaRepository;
        this.concertDateJpaRepository = concertDateJpaRepository1;
    }

    @Override
    public List<Concert> findAll() {
        return concertJpaRepository.findAll();
    }

    @Override
    public Concert findById(Long concertId) {
        return concertJpaRepository.findById(concertId).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public ConcertDate findConcertDateById(Long concertDateId) {
        return concertDateJpaRepository.findById(concertDateId).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public void addConcertDates(List<ConcertDate> concertDates) {
        concertDateJpaRepository.saveAll(concertDates);
    }

    @Override
    public void addConcert(Concert concert) {
        concertJpaRepository.save(concert);
    }

    @Override
    public void deleteAll() {
        concertDateJpaRepository.deleteAll();
        concertJpaRepository.deleteAll();
    }

    @Override
    public void deleteAllDates() {
        concertDateJpaRepository.deleteAll();
    }
}

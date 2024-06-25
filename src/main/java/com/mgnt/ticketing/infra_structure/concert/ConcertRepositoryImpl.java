package com.mgnt.ticketing.infra_structure.concert;


import com.mgnt.ticketing.domain.concert.dto.GetSeatsQueryResDto;
import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.repository.ConcertDateJpaRepository;
import com.mgnt.ticketing.domain.concert.repository.ConcertJpaRepository;
import com.mgnt.ticketing.domain.concert.repository.ConcertQueryDslRepository;
import com.mgnt.ticketing.domain.concert.repository.ConcertRepository;

import java.util.List;
import java.util.NoSuchElementException;

public class ConcertRepositoryImpl implements ConcertRepository {

    private ConcertJpaRepository concertJpaRepository;
    private ConcertDateJpaRepository concertDateJpaRepository;
    private ConcertQueryDslRepository concertQueryDslRepository;

    @Override
    public List<Concert> findAll() {
        return concertJpaRepository.findAll();
    }

    @Override
    public Concert findById(Long concertId) {
        return concertJpaRepository.findById(concertId).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public List<GetSeatsQueryResDto> getSeatsByConcertDate(Long concertId, Long concertDateId) {
        return concertQueryDslRepository.getSeatsByConcertDate(concertId, concertDateId);
    }
}
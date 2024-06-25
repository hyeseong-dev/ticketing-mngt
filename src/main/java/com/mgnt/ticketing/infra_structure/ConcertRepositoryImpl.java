package com.mgnt.ticketing.infra_structure;

import com.mgnt.ticketing.domain.concert.repository.ConcertDateJpaRepository;
import com.mgnt.ticketing.domain.concert.repository.ConcertJpaRepository;
import com.mgnt.ticketing.domain.concert.repository.ConcertRepository;
import com.mgnt.ticketing.domain.concert.repository.SeatJpaRepository;

public class ConcertRepositoryImpl implements ConcertRepository {

    private ConcertJpaRepository concertJpaRepository;
    private ConcertDateJpaRepository concertDateJpaRepository;
    private SeatJpaRepository seatJpaRepository;
}

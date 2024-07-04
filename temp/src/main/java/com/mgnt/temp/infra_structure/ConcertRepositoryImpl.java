package com.mgnt.temp.infra_structure;


import com.mgnt.temp.domain.entity.ConcertDate;
import com.mgnt.temp.domain.entity.Seat;
import com.mgnt.temp.domain.repository.ConcertDateJpaRepository;
import com.mgnt.temp.domain.repository.ConcertJpaRepository;
import com.mgnt.temp.domain.repository.ConcertRepository;
import com.mgnt.temp.domain.repository.SeatJpaRepository;
import com.mgnt.ticketing.domain.concert.entity.Concert;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ConcertRepositoryImpl implements ConcertRepository {

    private final ConcertJpaRepository concertJpaRepository;
    private final ConcertDateJpaRepository concertDateJpaRepository;
    private final SeatJpaRepository seatJpaRepository;

    public ConcertRepositoryImpl(ConcertJpaRepository concertJpaRepository, ConcertDateJpaRepository concertDateJpaRepository, ConcertDateJpaRepository concertDateJpaRepository1, SeatJpaRepository seatJpaRepository) {
        this.concertJpaRepository = concertJpaRepository;
        this.concertDateJpaRepository = concertDateJpaRepository1;
        this.seatJpaRepository = seatJpaRepository;
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

    @Override
    public boolean existByConcertDateAndStatus(Long concertDateId, Seat.Status status) {
        return seatJpaRepository.existsByConcertDate_concertDateIdAndStatus(concertDateId, status);
    }

    @Override
    public List<Seat> findSeatsByConcertDateIdAndStatus(Long concertDateId, Seat.Status status) {
        return seatJpaRepository.findAllByConcertDate_concertDateIdAndStatus(concertDateId, status);
    }

    @Override
    public Seat findSeatByConcertDateIdAndSeatNum(Long concertDateId, int seatNum) {
        return seatJpaRepository.findSeatByConcertDate_concertDateIdAndSeatNum(concertDateId, seatNum);
    }

    @Override
    public void addSeats(List<Seat> seats) {
        seatJpaRepository.saveAll(seats);
    }
}


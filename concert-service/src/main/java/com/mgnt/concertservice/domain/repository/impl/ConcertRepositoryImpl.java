package com.mgnt.concertservice.domain.repository.impl;

import com.mgnt.concertservice.domain.entity.Concert;
import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.core.enums.SeatStatus;
import com.mgnt.concertservice.domain.repository.ConcertRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Optional<Concert> findByConcertId(Long concertId) {
        return Optional.ofNullable(entityManager.find(Concert.class, concertId));
    }

    @Override
    public List<Concert> findAllWithPlace() {
        // 적절한 JPQL 또는 Criteria API를 사용하여 구현
        return null;
    }

    @Override
    public Optional<Concert> findByConcertIdWithPlace(Long concertId) {
        // 적절한 JPQL 또는 Criteria API를 사용하여 구현
        return null;
    }

    @Override
    public List<Seat> findSeatsByConcertDateIdAndStatus(Long concertDateId, SeatStatus status) {
        // 적절한 JPQL 또는 Criteria API를 사용하여 구현
        return null;
    }

    @Override
    public boolean existsByConcertDateAndStatus(Long concertDateId, SeatStatus status) {
        // 적절한 JPQL 또는 Criteria API를 사용하여 구현
        return false;
    }

    @Override
    public Optional<Seat> findSeatByConcertDateIdAndSeatId(Long concertDateId, Long seatId) {
        // 적절한 JPQL 또는 Criteria API를 사용하여 구현
        return null;
    }
}

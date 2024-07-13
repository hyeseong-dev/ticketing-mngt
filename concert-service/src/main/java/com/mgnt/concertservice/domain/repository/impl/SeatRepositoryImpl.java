package com.mgnt.concertservice.domain.repository.impl;

import com.mgnt.concertservice.domain.entity.QSeat;
import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.concertservice.domain.repository.SeatRepositoryCustom;
import com.mgnt.core.enums.SeatStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SeatRepositoryImpl implements SeatRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    @Override
    public Optional<Seat> findAndLockByConcertDateIdAndSeatId(Long concertDateId, Long seatId) {
        QSeat seat = QSeat.seat;
        Seat foundSeat = queryFactory
                .selectFrom(seat)
                .where(seat.concertDateId.eq(concertDateId)
                        .and(seat.seatId.eq(seatId)))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne();

        if (foundSeat != null) {
            entityManager.lock(foundSeat, LockModeType.PESSIMISTIC_WRITE);
        }

        return Optional.ofNullable(foundSeat);
    }

    @Override
    public Optional<Seat> findSeatByConcertDateIdAndSeatId(Long concertDateId, Long seatId) {
        return Optional.empty();
    }

    @Override
    public Optional<Seat> findByConsertDateIdAndSeatId(Long concertDateId, Long seatId) {
        QSeat seat = QSeat.seat;
        Seat foundSeat = queryFactory
                .selectFrom(seat)
                .where(seat.concertDateId.eq(concertDateId)
                        .and(seat.seatId.eq(seatId)))
                .fetchOne();

        return Optional.ofNullable(foundSeat);
    }

    @Override
    public Optional<Seat> findAvailableSeatByConcertDateIdAndSeatId(Long concertDateId, Long seatId) {
        QSeat seat = QSeat.seat;
        Seat foundSeat = queryFactory
                .selectFrom(seat)
                .where(seat.concertDateId.eq(concertDateId)
                        .and(seat.seatId.eq(seatId))
                        .and(seat.status.eq(SeatStatus.AVAILABLE)))
                .fetchOne();

        return Optional.ofNullable(foundSeat);
    }

    @Override
    public int updateSeatStatus(Long concertDateId, Long seatId, SeatStatus seatStatus) {
        if (concertDateId == null || seatId == null || seatStatus == null) {
            throw new IllegalArgumentException("concertDateId, seatId, and seatStatus must not be null");
        }

        QSeat seat = QSeat.seat;

        try {
            long updatedCount = queryFactory
                    .update(seat)
                    .set(seat.status, seatStatus)
                    .where(seat.concertDateId.eq(concertDateId)
                            .and(seat.seatId.eq(seatId)))
                    .execute();

            log.info("Updated seat status: concertDateId={}, seatId={}, newStatus={}, updatedCount={}",
                    concertDateId, seatId, seatStatus, updatedCount);

            return (int) updatedCount;
        } catch (Exception e) {
            log.error("Failed to update seat status: concertDateId={}, seatId={}, newStatus={}",
                    concertDateId, seatId, seatStatus, e);
            throw new RuntimeException("Failed to update seat status", e);
        }
    }
}
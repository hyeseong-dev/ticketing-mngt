package com.mgnt.concertservice.domain.repository;

import com.mgnt.concertservice.domain.entity.*;
import com.mgnt.core.enums.SeatStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ConcertRepositoryCustomImpl implements ConcertRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ConcertRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Optional<Concert> findByConcertId(Long concertId) {
        QConcert concert = QConcert.concert;
        Concert result = queryFactory
                .selectFrom(concert)
                .where(concert.concertId.eq(concertId))
                .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public List<Concert> findAllWithPlace() {
        QConcert concert = QConcert.concert;
        QPlace place = QPlace.place;

        return queryFactory
                .selectFrom(concert)
                .leftJoin(place).on(concert.placeId.eq(place.placeId))
                .fetch();
    }

    @Override
    public Optional<Concert> findByConcertIdWithPlace(Long concertId) {
        QConcert concert = QConcert.concert;
        QPlace place = QPlace.place;

        Concert result = queryFactory
                .selectFrom(concert)
                .leftJoin(place).on(concert.placeId.eq(place.placeId))
                .where(concert.concertId.eq(concertId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<Seat> findSeatsByConcertDateIdAndStatus(Long concertDateId, SeatStatus status) {
        QSeat seat = QSeat.seat;

        return queryFactory
                .selectFrom(seat)
                .where(seat.concertDateId.eq(concertDateId)
                        .and(seat.status.eq(status)))
                .fetch();
    }

    @Override
    public boolean existsByConcertDateAndStatus(Long concertDateId, SeatStatus status) {
        QSeat seat = QSeat.seat;

        return queryFactory
                .selectOne()
                .from(seat)
                .where(seat.concertDateId.eq(concertDateId)
                        .and(seat.status.eq(status)))
                .fetchFirst() != null;
    }

    @Override
    public Optional<Seat> findSeatByConcertDateIdAndSeatId(Long concertDateId, Long seatId) {
        QSeat seat = QSeat.seat;

        Seat result = queryFactory
                .selectFrom(seat)
                .where(seat.concertDateId.eq(concertDateId)
                        .and(seat.seatId.eq(seatId)))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}

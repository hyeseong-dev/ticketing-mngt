package com.mgnt.concertservice.domain.repository;

;
import com.mgnt.concertservice.domain.entity.QSeat;
import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.core.enums.SeatStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
public class SeatRepositoryCustomImpl implements SeatRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public SeatRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
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

package com.mgnt.reservationservice.domain.repository;

import com.mgnt.reservationservice.domain.entity.QReservation;
import com.mgnt.reservationservice.domain.entity.Reservation;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ReservationRepositoryCustomImpl implements ReservationRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Reservation> findAllByUserId(Long userId) {
        QReservation reservation = QReservation.reservation;

        return queryFactory.selectFrom(reservation)
                .where(reservation.userId.eq(userId))
                .fetch();
    }

    @Override
    public Optional<Reservation> findByReservationId(Long reservationId) {
        QReservation reservation = QReservation.reservation;

        Reservation result = queryFactory.selectFrom(reservation)
                .where(reservation.reservationId.eq(reservationId))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}

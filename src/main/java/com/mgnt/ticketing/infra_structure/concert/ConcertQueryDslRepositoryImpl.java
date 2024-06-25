package com.mgnt.ticketing.infra_structure.concert;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.mgnt.ticketing.domain.concert.dto.GetSeatsQueryResDto;
import com.mgnt.ticketing.domain.concert.dto.QGetSeatsQueryResDto;
import com.mgnt.ticketing.domain.concert.repository.ConcertQueryDslRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;


import static com.mgnt.ticketing.domain.concert.entity.QSeat.seat;
import static com.mgnt.ticketing.domain.reservation.entity.QReservation.reservation;

@RequiredArgsConstructor
public class ConcertQueryDslRepositoryImpl implements ConcertQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<GetSeatsQueryResDto> getSeatsByConcertDate(Long concertId, Long concertDateId) {
        queryFactory.select(new QGetSeatsQueryResDto(
                        seat.seatId,
                        seat.seatNum
                ))
                .from(seat)
                .join(reservation).on(reservation.seat.seatId.eq(seat.seatId))
                // left join 외않되?
                // TODO 의존성 왜 이래 빈 생성 실행 안돼
                .where(reservation.concertDate.concertDateId.eq(concertDateId))
                .fetch();
        return List.of();
    }
}
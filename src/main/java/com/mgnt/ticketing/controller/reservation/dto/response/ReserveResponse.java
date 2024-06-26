package com.mgnt.ticketing.controller.reservation.dto.response;

import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import lombok.Builder;

import java.time.ZonedDateTime;

/**
 * 예약 응답 DTO
 */
public record ReserveResponse(
        Long reservationId,
        Reservation.Status status,
        ConcertInfo concertInfo
) {

    @Builder
    public ReserveResponse {
    }

    public static ReserveResponse from(Reservation reservation, Concert concert, ConcertDate concertDate, Seat seat) {
        return ReserveResponse.builder()
                .reservationId(reservation.getReservationId())
                .status(reservation.getStatus())
                .concertInfo(ConcertInfo.builder()
                        .concertId(concert.getConcertId())
                        .concertDateId(concertDate.getConcertDateId())
                        .name(concert.getName())
                        .date(concertDate.getConcertDate())
                        .seatId(seat.getSeatId())
                        .seatNum(seat.getSeatNum())
                        .build())
                .build();
    }

    @Builder
    public static record ConcertInfo(
            Long concertId,
            Long concertDateId,
            String name,
            ZonedDateTime date,
            Long seatId,
            int seatNum
    ) {
    }
}
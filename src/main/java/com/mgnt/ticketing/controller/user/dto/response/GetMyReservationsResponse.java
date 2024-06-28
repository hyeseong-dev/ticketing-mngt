package com.mgnt.ticketing.controller.user.dto.response;

import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.reservation.service.dto.GetReservationAndPaymentResDto;
import lombok.Builder;

import java.time.ZonedDateTime;

public record GetMyReservationsResponse(

        Long reservationId,
        Reservation.Status status,
        ConcertInfo concertInfo
) {
    @Builder
    public GetMyReservationsResponse {
    }

    public static GetMyReservationsResponse from(GetReservationAndPaymentResDto resDto) {
        if (resDto == null || resDto.reservation() == null) {
            return null;
        }

        Reservation reservation = resDto.reservation();
        Concert concertInfo = resDto.concert();
        ConcertDate concertDateInfo = resDto.concertDate();
        Seat seatInfo = resDto.seat();

        return GetMyReservationsResponse.builder()
                .reservationId(reservation.getReservationId())
                .status(reservation.getStatus())
                .concertInfo(GetMyReservationsResponse.ConcertInfo.builder()
                        .concertId(concertInfo.getConcertId())
                        .concertDateId(concertDateInfo.getConcertDateId())
                        .name(concertInfo.getName())
                        .date(concertDateInfo.getConcertDate())
                        .seatId(seatInfo.getSeatId())
                        .seatNum(seatInfo.getSeatNum())
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
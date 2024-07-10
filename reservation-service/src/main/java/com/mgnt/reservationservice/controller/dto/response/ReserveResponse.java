package com.mgnt.reservationservice.controller.dto.response;

import com.mgnt.core.event.SeatDTO;
import com.mgnt.reservationservice.domain.entity.Reservation;
import com.mgnt.reservationservice.domain.service.dto.ConcertDateDTO;
import com.mgnt.reservationservice.domain.service.dto.ConcertInfoDTO;
import lombok.Builder;

import java.time.ZonedDateTime;

public record ReserveResponse(
        Long reservationId,
        Reservation.Status status,
        ConcertInfo concertInfo
) {

    @Builder
    public ReserveResponse {
    }

    public static ReserveResponse from(Reservation reservation, ConcertInfoDTO concert, ConcertDateDTO concertDate, SeatDTO seat) {
        return ReserveResponse.builder()
                .reservationId(reservation.getReservationId())
                .status(reservation.getStatus())
                .concertInfo(ConcertInfo.builder()
                        .concertId(concert.concertId())
                        .concertDateId(concertDate.concertDateId())
                        .name(concert.name())
                        .date(concertDate.concertDate())
                        .seatId(seat.seatId())
                        .build())
                .build();
    }

    @Builder
    public static record ConcertInfo(
            Long concertId,
            Long concertDateId,
            String name,
            ZonedDateTime date,
            Long seatId
    ) {
    }
}

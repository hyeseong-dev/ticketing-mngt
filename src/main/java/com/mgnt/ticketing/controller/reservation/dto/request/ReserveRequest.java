package com.mgnt.ticketing.controller.reservation.dto.request;

import com.mgnt.ticketing.domain.concert.service.ConcertReader;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.user.service.UserReader;
import jakarta.validation.constraints.NotNull;

public record ReserveRequest(
        @NotNull Long concertId,
        @NotNull Long concertDateId,
        @NotNull int seatNum,
        @NotNull Long userId
) {

    public Reservation toEntity(ConcertReader concertReader, UserReader userReader) {
        return Reservation.builder()
                .concertId(concertId)
                .concertDateId(concertDateId)
                .seatNum(seatNum)
                .userId(userId)
                .status(Reservation.Status.ING)
                .build();
    }
}

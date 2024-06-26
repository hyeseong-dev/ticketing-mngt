package com.mgnt.ticketing.controller.reservation.dto.request;

import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.concert.service.ConcertReader;
import com.mgnt.ticketing.domain.reservation.ReservationEnums;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.user.entity.User;
import com.mgnt.ticketing.domain.user.service.UserReader;
import jakarta.validation.constraints.NotNull;

public record ReserveRequest(
        @NotNull Long concertId,
        @NotNull Long concertDateId,
        @NotNull Long seatId,
        @NotNull Long userId
) {

    public Reservation toEntity(ConcertReader concertReader, UserReader userReader) {
        Concert concert = concertReader.getConcert(concertId);
        ConcertDate concertDate = concertReader.getConcertDate(concertDateId);
        Seat seat = concertReader.getSeat(seatId);
        User user = userReader.getUser(userId);

        return Reservation.builder()
                .concert(concert)
                .concertDate(concertDate)
                .seat(seat)
                .user(user)
                .status(ReservationEnums.Status.ING)
                .build();
    }
}

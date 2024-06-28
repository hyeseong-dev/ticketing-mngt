package com.mgnt.ticketing.controller.reservation.dto.request;

import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.concert.service.ConcertReader;
import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.payment.service.dto.CreatePaymentReqDto;
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
        Concert concert = concertReader.findConcert(concertId);
        ConcertDate concertDate = concertReader.findConcertDate(concertDateId);
        Seat seat = concertReader.findSeat(seatId);
        User user = userReader.findUser(userId);

        return Reservation.builder()
                .concert(concert)
                .concertDate(concertDate)
                .seat(seat)
                .user(user)
                .status(Reservation.Status.ING)
                .build();
    }

    public CreatePaymentReqDto toCreatePayment(Reservation reservation) {
        return new CreatePaymentReqDto(reservation, Payment.Status.READY, reservation.getSeat().getPrice());
    }
}

package com.mgnt.reservationservice.domain.service.dto;

import com.mgnt.reservationservice.domain.entity.Reservation;

public record GetReservationAndPaymentResDto(
        Reservation reservation,
        Concert concert,
        ConcertDate concertDate,
        Seat seat
) {
}
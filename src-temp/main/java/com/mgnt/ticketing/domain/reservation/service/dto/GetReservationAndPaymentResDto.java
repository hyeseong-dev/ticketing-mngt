package com.mgnt.ticketing.domain.reservation.service.dto;

import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;


public record GetReservationAndPaymentResDto(
        Reservation reservation,
        Concert concert,
        ConcertDate concertDate,
        Seat seat
) {
}
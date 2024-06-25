package com.mgnt.ticketing.controller.user.dto.response;

import com.mgnt.ticketing.domain.payment.PaymentEnums;
import com.mgnt.ticketing.domain.reservation.ReservationEnums;
import lombok.Builder;

import java.time.ZonedDateTime;

public record GetMyReservationsResponse(

        Long reservationId,
        ReservationEnums.Status status,
        ConcertInfo concertInfo,
        PaymentInfo paymentInfo
) {
    @Builder
    public GetMyReservationsResponse {
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

    @Builder
    public static record PaymentInfo(
            Long paymentId,
            PaymentEnums.Status status,
            int paymentPrice
    ) {
    }

}

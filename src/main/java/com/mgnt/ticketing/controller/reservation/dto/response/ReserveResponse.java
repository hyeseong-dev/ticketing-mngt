package com.mgnt.ticketing.controller.reservation.dto.response;

import com.mgnt.ticketing.domain.payment.PaymentEnums;
import com.mgnt.ticketing.domain.reservation.ReservationEnums;
import lombok.Builder;

import java.time.ZonedDateTime;

public record ReserveResponse(
        Long reservationId,
        ReservationEnums.Status status,
        ConcertInfo concertInfo,
        PaymentInfo paymentInfo
) {

    @Builder
    public ReserveResponse {
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

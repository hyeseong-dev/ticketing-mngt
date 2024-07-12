package com.mgnt.core.event.concert_service;

import java.math.BigDecimal;

public record SeatReservationResponseEvent(
        Long reservationId,
        boolean isSuccess,
        BigDecimal price
) {
}

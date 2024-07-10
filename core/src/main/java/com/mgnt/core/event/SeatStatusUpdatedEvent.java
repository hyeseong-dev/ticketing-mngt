package com.mgnt.core.event;

import java.math.BigDecimal;

public record SeatStatusUpdatedEvent(
        Long reservationId,
        Long userId,
        Long concertId,
        Long concertDateId,
        Long seatId,
        BigDecimal price
) implements Event {
}
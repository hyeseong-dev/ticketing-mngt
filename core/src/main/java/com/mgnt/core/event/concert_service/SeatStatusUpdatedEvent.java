package com.mgnt.core.event.concert_service;

import com.mgnt.core.event.Event;

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
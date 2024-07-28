package com.mgnt.core.event.concert_service;

import com.mgnt.core.enums.SeatStatus;
import com.mgnt.core.event.Event;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record SeatStatusUpdatedEvent(
        Long reservationId,
        Long userId,
        Long concertId,
        Long concertDateId,
        Long seatId,
        BigDecimal price,
        SeatStatus status
) implements Event {
}
package com.mgnt.core.event;

public record SeatStatusUpdatedEvent(
        Long reservationId,
        Long userId,
        Long concertId,
        Long concertDateId,
        int seatNum,
        boolean isAvailable
) implements Event {
}
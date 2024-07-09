package com.mgnt.core.event;

public record SeatStatusUpdatedEvent(
        Long reservationId,
        Long userId,
        Long concertId,
        Long concertDateId,
        Long seatId,
        boolean isAvailable
) implements Event {
}
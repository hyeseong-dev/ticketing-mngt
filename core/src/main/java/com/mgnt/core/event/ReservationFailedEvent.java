package com.mgnt.core.event;

public record ReservationFailedEvent(
        Long reservationId,
        Long concertDateId,
        Long seatId
) implements Event {
}
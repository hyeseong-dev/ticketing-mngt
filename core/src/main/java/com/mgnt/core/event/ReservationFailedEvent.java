package com.mgnt.core.event;

public record ReservationFailedEvent(
        Long reservationId,
        Long concertDateId,
        int seatNum
) implements Event {
}
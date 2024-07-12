package com.mgnt.core.event.reservation_service;

import com.mgnt.core.event.Event;

public record ReservationFailedEvent(
        Long reservationId,
        Long concertDateId,
        Long seatId
) implements Event {
}
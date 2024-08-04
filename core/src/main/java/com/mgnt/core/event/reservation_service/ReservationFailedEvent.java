package com.mgnt.core.event.reservation_service;

import com.mgnt.core.event.Event;

public record ReservationFailedEvent(
        Long userId,
        Long reservationId,
        Long concertDateId,
        Long seatId,
        String reason
) implements Event {
}
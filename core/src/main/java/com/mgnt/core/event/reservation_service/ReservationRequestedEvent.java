package com.mgnt.core.event.reservation_service;

import com.mgnt.core.event.Event;

public record ReservationRequestedEvent(
        Long concertDateId,
        Long userId,
        Long concertId,
        Long seatId
) implements Event {
}

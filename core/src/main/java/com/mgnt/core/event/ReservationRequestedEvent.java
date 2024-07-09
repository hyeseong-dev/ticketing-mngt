package com.mgnt.core.event;

public record ReservationRequestedEvent(
        Long concertDateId,
        Long userId,
        Long concertId,
        Long seatId
) implements Event {
}

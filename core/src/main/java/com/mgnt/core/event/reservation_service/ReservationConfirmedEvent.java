package com.mgnt.core.event.reservation_service;

import com.mgnt.core.enums.SeatStatus;
import com.mgnt.core.event.Event;

public record ReservationConfirmedEvent(
        Long reservationId,
        Long concertDateId,
        Long seatId,
        SeatStatus status
) implements Event {
}
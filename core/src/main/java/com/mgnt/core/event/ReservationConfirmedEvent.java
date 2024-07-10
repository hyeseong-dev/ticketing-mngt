package com.mgnt.core.event;

import com.mgnt.core.enums.SeatStatus;

public record ReservationConfirmedEvent(
        Long reservationId,
        Long concertDateId,
        Long seatId,
        SeatStatus status
) implements Event {
}
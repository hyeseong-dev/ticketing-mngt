package com.mgnt.core.event.concert_service;

import com.mgnt.core.event.Event;

public record ConcertInfoRequestEvent(
        Long reservationId,
        Long concertId,
        Long concertDateId,
        Long seatId
) implements Event {
}
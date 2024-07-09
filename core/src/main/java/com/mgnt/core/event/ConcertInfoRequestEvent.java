package com.mgnt.core.event;

public record ConcertInfoRequestEvent(
        Long reservationId,
        Long concertId,
        Long concertDateId,
        Long seatId
) implements Event {
}
package com.mgnt.core.event;

public record ConcertInfoRequestEvent(
        Long reservationId,
        Long concertId,
        Long concertDateId,
        int seatNum
) implements Event {
}
package com.mgnt.core.event;

public record ConcertInfoResponseEvent(
        Long reservationId,
        ConcertInfoDTO concertInfo,
        ConcertDateDTO concertDate,
        SeatDTO seat
) implements Event {
}
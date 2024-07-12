package com.mgnt.core.event;

public record ConcertInfoResponseEvent(
        Long reservationId,
        ConcertInfoDTO concertInfo
) implements Event {
}
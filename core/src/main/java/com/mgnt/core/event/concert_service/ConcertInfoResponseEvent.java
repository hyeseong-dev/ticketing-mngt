package com.mgnt.core.event.concert_service;

import com.mgnt.core.event.Event;

public record ConcertInfoResponseEvent(
        Long reservationId,
        ConcertInfoDTO concertInfo
) implements Event {
}
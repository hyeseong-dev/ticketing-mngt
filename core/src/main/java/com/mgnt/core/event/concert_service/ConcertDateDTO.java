package com.mgnt.core.event.concert_service;

import java.time.ZonedDateTime;

public record ConcertDateDTO(
        Long concertDateId,
        ZonedDateTime concertDate
) {
}

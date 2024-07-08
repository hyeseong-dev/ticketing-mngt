package com.mgnt.core.event;

import java.time.ZonedDateTime;

public record ConcertDateDTO(
        Long concertDateId,
        ZonedDateTime concertDate
) {
}

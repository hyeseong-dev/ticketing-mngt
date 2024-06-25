package com.mgnt.ticketing.controller.concert.dto.response;

import java.time.ZonedDateTime;

public record GetDatesResponse(
        Long concertDateId,
        ZonedDateTime date,
        boolean isSoldOut
) {
}

package com.mgnt.ticketing.controller.concert.dto.response;

import java.time.ZonedDateTime;

public record GetConcertsResponse(
        Long concertId,
        String name,
        ZonedDateTime createdAt
) {
}

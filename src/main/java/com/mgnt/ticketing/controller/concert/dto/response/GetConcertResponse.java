package com.mgnt.ticketing.controller.concert.dto.response;

import lombok.Builder;

import java.time.ZonedDateTime;

public record GetConcertResponse(
        Long concertId,
        String name,
        String hall,
        String period,
        String price,
        ZonedDateTime createdAt
) {
    @Builder
    public GetConcertResponse {
    }
}

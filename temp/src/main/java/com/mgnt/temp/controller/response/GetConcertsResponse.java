package com.mgnt.temp.controller.response;

import com.mgnt.ticketing.domain.concert.entity.Concert;

import java.time.ZonedDateTime;

public record GetConcertsResponse(
        Long concertId,
        String name,
        String place,
        ZonedDateTime createdAt
) {
    public static GetConcertsResponse from(Concert concert) {
        return new GetConcertsResponse(
                concert.getConcertId(),
                concert.getName(),
                concert.getPlace() != null ? concert.getPlace().getName() : "-",
                concert.getCreatedAt()
        );
    }
}

package com.mgnt.concertservice.controller.response;

import com.mgnt.concertservice.domain.entity.Concert;

import java.time.ZonedDateTime;

public record GetConcertsResponse(
        Long concertId,
        String concertName,
        String placeName,
        ZonedDateTime createdAt
) {
    public static GetConcertsResponse of(Concert concert, String placeName) {
        return new GetConcertsResponse(
                concert.getConcertId(),
                concert.getName(),
                placeName,
                concert.getCreatedAt()
        );
    }
}
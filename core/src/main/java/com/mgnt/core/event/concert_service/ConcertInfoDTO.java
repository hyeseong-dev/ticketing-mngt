package com.mgnt.core.event.concert_service;

public record ConcertInfoDTO(
        Long concertId,
        String name,
        PlaceDTO place,
        ConcertDateDTO concertDate,
        SeatDTO seat
) {
}
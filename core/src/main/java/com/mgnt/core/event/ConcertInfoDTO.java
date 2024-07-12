package com.mgnt.core.event;

public record ConcertInfoDTO(
        Long concertId,
        String name,
        PlaceDTO place,
        ConcertDateDTO concertDate,
        SeatDTO seat
) {
}
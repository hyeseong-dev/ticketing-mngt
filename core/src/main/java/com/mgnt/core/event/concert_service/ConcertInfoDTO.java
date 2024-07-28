package com.mgnt.core.event.concert_service;

import com.mgnt.core.dto.SeatDTO;

public record ConcertInfoDTO(
        Long concertId,
        String name,
        PlaceDTO place,
        ConcertDateDTO concertDate,
        SeatDTO seat
) {
}
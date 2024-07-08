package com.mgnt.reservationservice.domain.service.dto;

import java.time.ZonedDateTime;

public record ConcertDateDTO(
        Long concertDateId,
        ZonedDateTime concertDate
) {
}
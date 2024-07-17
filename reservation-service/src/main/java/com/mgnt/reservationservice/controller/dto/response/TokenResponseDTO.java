package com.mgnt.reservationservice.controller.dto.response;

import com.mgnt.core.event.reservation_service.QueueEventStatus;

import java.time.ZonedDateTime;

public record TokenResponseDTO(
        Long userId,
        Long concertId,
        Long concertDateId,
        QueueEventStatus status,
        Long position,
        String token,
        ZonedDateTime expiryTime
) {
}

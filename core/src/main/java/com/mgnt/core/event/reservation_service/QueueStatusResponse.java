package com.mgnt.core.event.reservation_service;

public record QueueStatusResponse(
        Long userId,
        Long concertId,
        Long concertDateId,
        QueueEventStatus status,
        Long position
) {
}

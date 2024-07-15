package com.mgnt.core.event.reservation_service;

public record QueueEntryResponse(
        Long userId,
        Long position,
        Long concertId,
        Long concertDateId
) {
}

package com.mgnt.core.event.reservation_service;

public record QueueEntryRequest(
        Long concertId,
        Long concertDateId
) {
}

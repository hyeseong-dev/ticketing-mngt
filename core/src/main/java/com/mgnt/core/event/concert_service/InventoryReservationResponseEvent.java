package com.mgnt.core.event.concert_service;

public record InventoryReservationResponseEvent(
        Long concertId,
        Long concertDateId,
        boolean isSuccess
) {
}

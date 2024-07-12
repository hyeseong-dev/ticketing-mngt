package com.mgnt.core.event.concert_service;

public record InventoryReservationRequestEvent(
        Long concertId,
        Long concertDateId
) {
}
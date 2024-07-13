package com.mgnt.core.event.concert_service;

public record InventoryReservationRequestEvent(
        Long reservationId,
        Long concertId,
        Long concertDateId,
        boolean isSuccess
) {
}
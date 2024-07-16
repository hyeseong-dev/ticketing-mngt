package com.mgnt.core.event.reservation_service;

public record ReservationAccessGrantedEvent(
        Long userId,
        Long concertId,
        Long concertDateId,
        String accessToken
) {
}

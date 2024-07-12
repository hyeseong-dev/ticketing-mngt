package com.mgnt.core.event.reservation_service;

import com.mgnt.core.event.Event;

import java.math.BigDecimal;

public record ReservationCreatedEvent(
        Long reservationId,
        Long userId,
        BigDecimal price
) implements Event {
}
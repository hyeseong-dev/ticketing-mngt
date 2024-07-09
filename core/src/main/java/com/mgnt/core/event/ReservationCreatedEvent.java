package com.mgnt.core.event;

import java.math.BigDecimal;

public record ReservationCreatedEvent(
        Long reservationId,
        Long userId,
        BigDecimal price
) implements Event {
}
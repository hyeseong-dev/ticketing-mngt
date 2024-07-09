package com.mgnt.core.event;

import java.math.BigDecimal;

public record PaymentCreatedEvent(
        Long paymentId,
        Long reservationId,
        Long userId,
        BigDecimal price
) implements Event {
}
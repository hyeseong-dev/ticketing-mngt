package com.mgnt.core.event;

import java.math.BigDecimal;

public record PaymentCreatedEvent(
        Long paymentId,
        Long reservationId,
        BigDecimal price
) implements Event {
}
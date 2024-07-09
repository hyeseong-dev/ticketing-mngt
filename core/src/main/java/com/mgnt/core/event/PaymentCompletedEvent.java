package com.mgnt.core.event;

import java.math.BigDecimal;

public record PaymentCompletedEvent(
        Long paymentId,
        Long reservationId,
        Long userId,
        BigDecimal price,
        boolean isSuccess,
        BigDecimal usedBalance
) implements Event {
}
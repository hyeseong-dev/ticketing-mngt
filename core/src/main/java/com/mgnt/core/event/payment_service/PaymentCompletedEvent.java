package com.mgnt.core.event.payment_service;

import com.mgnt.core.event.Event;

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
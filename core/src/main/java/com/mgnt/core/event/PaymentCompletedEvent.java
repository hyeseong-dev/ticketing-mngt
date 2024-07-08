package com.mgnt.core.event;

import java.math.BigDecimal;

public record PaymentCompletedEvent(
        Long paymentId,
        Long reservationId,
        Long concertDateId,
        int seatNum,
        boolean isSuccess,
        BigDecimal usedBalance) implements Event {
}
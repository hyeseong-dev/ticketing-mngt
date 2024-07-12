package com.mgnt.core.event.payment_service;

import com.mgnt.core.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record PaymentInfoDTO(
        Long paymentId,
        BigDecimal price,
        PaymentStatus status,
        ZonedDateTime paidAt
) {
}

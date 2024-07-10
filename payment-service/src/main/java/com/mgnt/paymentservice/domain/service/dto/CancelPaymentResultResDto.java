package com.mgnt.paymentservice.domain.service.dto;

import com.mgnt.core.enums.PaymentStatus;
import com.mgnt.paymentservice.domain.entity.Payment;

public record CancelPaymentResultResDto(
        boolean isSuccess,
        Long paymentId,
        PaymentStatus status
) {
}
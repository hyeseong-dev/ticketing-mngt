package com.mgnt.paymentservice.domain.service.dto;

import com.mgnt.paymentservice.domain.entity.Payment;

public record CancelPaymentResultResDto(
        boolean isSuccess,
        Long paymentId,
        Payment.Status status
) {
}
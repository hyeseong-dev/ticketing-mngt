package com.mgnt.ticketing.domain.payment.service.dto;

import com.mgnt.ticketing.domain.payment.entity.Payment;

public record CancelPaymentResultResDto(
        boolean isSuccess,
        Long paymentId,
        Payment.Status status
) {
}
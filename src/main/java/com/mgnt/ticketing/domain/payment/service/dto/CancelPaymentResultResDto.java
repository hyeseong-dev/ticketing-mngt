package com.mgnt.ticketing.domain.payment.service.dto;

import com.mgnt.ticketing.domain.payment.PaymentEnums;

public record CancelPaymentResultResDto(
        boolean isSuccess,
        Long paymentId,
        PaymentEnums.Status status
) {
}
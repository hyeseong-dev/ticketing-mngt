package com.mgnt.paymentservice.controller.dto.response;

import com.mgnt.ticketing.domain.payment.entity.Payment;
import lombok.Builder;

import java.math.BigDecimal;

public record PayResponse(
        boolean isSuccess,
        Long paymentId,
        Payment.Status status,
        BigDecimal paymentPrice,
        BigDecimal balance
) {
    @Builder
    public PayResponse {
    }

    public static PayResponse from(boolean isSuccess, Payment paymentResult, BigDecimal usedBalance) {
        return PayResponse.builder()
                .isSuccess(isSuccess)
                .paymentId(paymentResult.getPaymentId())
                .status(paymentResult.getStatus())
                .paymentPrice(paymentResult.getPrice())
                .balance(usedBalance)
                .build();
    }
}


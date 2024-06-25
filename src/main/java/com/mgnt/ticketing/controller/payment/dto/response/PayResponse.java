package com.mgnt.ticketing.controller.payment.dto.response;

import com.mgnt.ticketing.domain.payment.PaymentEnums;
import lombok.Builder;

public record PayResponse(
        Long paymentId,
        PaymentEnums.Status status,
        int paymentPrice,
        int balance
) {
    @Builder
    public PayResponse {
    }
}

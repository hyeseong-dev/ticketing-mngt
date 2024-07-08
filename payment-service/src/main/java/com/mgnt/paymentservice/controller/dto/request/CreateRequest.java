package com.mgnt.paymentservice.controller.dto.request;

import com.mgnt.paymentservice.domain.entity.Payment;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateRequest(
        @NotNull Long reservationId,
        @NotNull BigDecimal price
) {

    public Payment toEntity(Long reservationId) {
        return Payment.builder()
                .reservationId(reservationId)
                .status(Payment.Status.READY)
                .price(price)
                .build();
    }
}


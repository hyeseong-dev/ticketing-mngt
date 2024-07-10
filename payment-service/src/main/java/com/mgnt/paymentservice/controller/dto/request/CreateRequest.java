package com.mgnt.paymentservice.controller.dto.request;


import com.mgnt.core.enums.PaymentStatus;
import com.mgnt.core.enums.ReservationStatus;
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
                .status(PaymentStatus.READY)
                .price(price)
                .build();
    }
}


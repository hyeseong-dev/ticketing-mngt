package com.mgnt.ticketing.controller.payment.dto.request;

import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateRequest(
        @NotNull Long reservationId,
        @NotNull BigDecimal price
) {

    public Payment toEntity(Reservation reservation) {
        return Payment.builder()
                .reservation(reservation)
                .status(Payment.Status.READY)
                .price(price)
                .build();
    }
}


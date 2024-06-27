package com.mgnt.ticketing.domain.payment.service.dto;

import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;

import java.math.BigDecimal;

public record CreatePaymentReqDto(
        Reservation reservation,
        Payment.Status status,
        BigDecimal price
) {

    public Payment toEntity() {
        return new Payment(reservation, status, price);
    }
}
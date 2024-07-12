package com.mgnt.core.event.payment_service;

public record PaymentInfoResponseEvent(
        Long reservationId,
        PaymentInfoDTO paymentInfo
) {
}

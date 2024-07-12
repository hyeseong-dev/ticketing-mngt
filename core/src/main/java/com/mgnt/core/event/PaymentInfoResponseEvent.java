package com.mgnt.core.event;

public record PaymentInfoResponseEvent(
        Long reservationId,
        PaymentInfoDTO paymentInfo
) {
}

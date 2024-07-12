package com.mgnt.core.event;

public record PaymentInfoRequestEvent(
        Long reservationId
) implements Event {
}

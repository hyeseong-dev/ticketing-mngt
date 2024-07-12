package com.mgnt.core.event.payment_service;

import com.mgnt.core.event.Event;

public record PaymentInfoRequestEvent(
        Long reservationId
) implements Event {
}

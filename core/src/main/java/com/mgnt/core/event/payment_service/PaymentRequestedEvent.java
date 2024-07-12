package com.mgnt.core.event.payment_service;

import com.mgnt.core.event.Event;

public record PaymentRequestedEvent(Long paymentId, Long userId) implements Event {
}
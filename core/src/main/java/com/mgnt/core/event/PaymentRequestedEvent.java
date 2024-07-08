package com.mgnt.core.event;

public record PaymentRequestedEvent(Long paymentId, Long userId) implements Event {
}
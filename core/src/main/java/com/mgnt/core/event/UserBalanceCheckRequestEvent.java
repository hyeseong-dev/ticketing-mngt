package com.mgnt.core.event;

public record UserBalanceCheckRequestEvent(Long userId, Long paymentId) implements Event {
}

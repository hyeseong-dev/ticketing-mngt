package com.mgnt.core.event;

public record UserBalanceUpdateResponseEvent(Long userId, Long paymentId, boolean success) implements Event {
}

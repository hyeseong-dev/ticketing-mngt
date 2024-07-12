package com.mgnt.core.event.user_service;

import com.mgnt.core.event.Event;

public record UserBalanceCheckRequestEvent(Long userId, Long paymentId) implements Event {
}

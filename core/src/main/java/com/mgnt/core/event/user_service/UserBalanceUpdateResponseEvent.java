package com.mgnt.core.event.user_service;

import com.mgnt.core.event.Event;

public record UserBalanceUpdateResponseEvent(Long userId, Long paymentId, boolean success) implements Event {
}

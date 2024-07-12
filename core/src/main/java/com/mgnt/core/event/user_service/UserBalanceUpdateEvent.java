package com.mgnt.core.event.user_service;

import com.mgnt.core.event.Event;

import java.math.BigDecimal;

public record UserBalanceUpdateEvent(Long userId, Long paymentId, BigDecimal price) implements Event {
}

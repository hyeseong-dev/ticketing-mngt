package com.mgnt.core.event.user_service;

import com.mgnt.core.event.Event;

import java.math.BigDecimal;

public record UserBalanceUpdatedEvent(
        Long userId,
        BigDecimal newBalance
) implements Event {
}
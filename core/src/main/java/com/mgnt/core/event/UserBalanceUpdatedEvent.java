package com.mgnt.core.event;

import java.math.BigDecimal;

public record UserBalanceUpdatedEvent(
        Long userId,
        BigDecimal newBalance
) implements Event {
}
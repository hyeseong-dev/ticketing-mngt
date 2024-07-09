package com.mgnt.core.event;

import java.math.BigDecimal;

public record UserBalanceCheckResponseEvent(Long userId, Long paymentId, BigDecimal balance) implements Event {
}


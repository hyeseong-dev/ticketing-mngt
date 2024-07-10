package com.mgnt.core.event;

import java.math.BigDecimal;

public record UserBalanceUpdateEvent(Long userId, Long paymentId, BigDecimal price) implements Event {
}

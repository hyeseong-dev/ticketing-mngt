package com.mgnt.core.event;

import com.mgnt.core.enums.SeatStatus;

import java.math.BigDecimal;

public record SeatDTO(
        Long seatId,
        int seatNum,
        BigDecimal price,
        SeatStatus status
) {
}

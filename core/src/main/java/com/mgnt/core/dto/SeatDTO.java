package com.mgnt.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mgnt.core.enums.SeatStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SeatDTO(
        Long seatId,
        int seatNum,
        BigDecimal price,
        SeatStatus status
) {
    // 필요한 경우 추가 메서드를 여기에 정의할 수 있습니다.
}
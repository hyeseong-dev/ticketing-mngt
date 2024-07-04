package com.mgnt.ticketing.domain.concert.dto;

import com.querydsl.core.annotations.QueryProjection;

public record GetSeatsQueryResDto(
        Long seatId,
        int seatNum
        // 뭐 필요할지 생각해보자
) {

    @QueryProjection
    public GetSeatsQueryResDto {
    }
}
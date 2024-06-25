package com.mgnt.ticketing.controller.concert.dto.response;

public record GetSeatsResponse(
        Long seatId,
        int seatNum,
        boolean isReserved
) {
}

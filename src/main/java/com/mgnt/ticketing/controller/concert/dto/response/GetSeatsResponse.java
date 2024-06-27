package com.mgnt.ticketing.controller.concert.dto.response;

import com.mgnt.ticketing.domain.concert.entity.Seat;

import java.io.Serializable;
import java.util.List;

public record GetSeatsResponse(
        List<SeatResponse> seats

) implements Serializable {

    public static GetSeatsResponse from(List<Seat> allSeats, List<Long> reservedSeatIds) {
        return new GetSeatsResponse(allSeats.stream()
                .map(seat -> new SeatResponse(seat.getSeatId(), seat.getSeatNum(), reservedSeatIds.contains(seat.getSeatId())))
                .toList());
    }

    public record SeatResponse(
            Long seatId,
            int seatNum,
            boolean isReserved
    ) {
    }
}

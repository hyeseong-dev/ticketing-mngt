package com.mgnt.reservationservice.domain.event.dto.request;

import com.mgnt.reservationservice.domain.event.SeatStatus;

public record UpdateSeatStatusRequest(
        Long concertDateId,
        int seatNum,
        SeatStatus status
) {
}


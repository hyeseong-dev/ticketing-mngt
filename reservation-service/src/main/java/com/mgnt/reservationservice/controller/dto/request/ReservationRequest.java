package com.mgnt.reservationservice.controller.dto.request;

import com.mgnt.core.enums.ReservationStatus;
import com.mgnt.reservationservice.domain.entity.Reservation;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ReservationRequest(
        @NotNull Long concertId,
        @NotNull Long concertDateId,
        @NotNull Long seatId,
        @NotNull BigDecimal price
) {

    public Reservation toEntity() { // 예약 엔티티를 만들기 위해서는 concertId, concertDateId,
        return Reservation.builder()
                .concertId(concertId)  //
                .concertDateId(concertDateId)
                .seatId(seatId)
                .price(price)
                .status(ReservationStatus.ING)
                .build();
    }
}

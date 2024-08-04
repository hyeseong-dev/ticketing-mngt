package com.mgnt.reservationservice.controller.dto.request;

import com.mgnt.core.enums.ReservationStatus;
import com.mgnt.reservationservice.domain.entity.Reservation;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record ReservationInventoryRequest(
        Long userId,
        @NotNull Long concertId,
        @NotNull Long concertDateId,
        @NotNull Long seatId,
        @NotNull BigDecimal price,
        @NotNull ReservationStatus status,
        @NotNull ZonedDateTime expiresAt
) {

    public Reservation toEntity(Long userId) {
        return Reservation.builder()
                .userId(userId)
                .concertId(concertId)
                .concertDateId(concertDateId)
                .seatId(seatId)
                .price(price)
                .status(status)
                .reservedAt(ZonedDateTime.now())
                .expiresAt(expiresAt)
                .build();
    }
}
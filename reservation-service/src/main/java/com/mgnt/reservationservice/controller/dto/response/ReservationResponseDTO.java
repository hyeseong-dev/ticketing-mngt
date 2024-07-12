package com.mgnt.reservationservice.controller.dto.response;

import com.mgnt.core.enums.ReservationStatus;
import com.mgnt.reservationservice.domain.entity.Reservation;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record ReservationResponseDTO(
        Long reservationId,
        ReservationStatus status,
        Long userId,
        Long concertId,
        Long concertDateId,
        Long seatId,
        BigDecimal price,
        ZonedDateTime reservedAt
) {

    public static ReservationResponseDTO from(Reservation reservation) {
        return new ReservationResponseDTO(
                reservation.getReservationId(),
                reservation.getStatus(),
                reservation.getUserId(),
                reservation.getConcertId(),
                reservation.getConcertDateId(),
                reservation.getSeatId(),
                reservation.getPrice(),
                reservation.getReservedAt()
        );
    }
}
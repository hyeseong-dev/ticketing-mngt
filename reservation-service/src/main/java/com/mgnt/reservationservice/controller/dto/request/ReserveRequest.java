package com.mgnt.reservationservice.controller.dto.request;

import com.mgnt.core.enums.ReservationStatus;
import com.mgnt.reservationservice.domain.entity.Reservation;
import jakarta.validation.constraints.NotNull;

public record ReserveRequest(
        @NotNull Long concertId,
        @NotNull Long concertDateId,
        @NotNull Long seatId) {

    public Reservation toEntity() { // 예약 엔티티를 만들기 위해서는 concertId, concertDateId,
        return Reservation.builder()
                .concertId(concertId)  //
                .concertDateId(concertDateId)
                .seatId(seatId)
                .status(ReservationStatus.ING)
                .build();
    }
}

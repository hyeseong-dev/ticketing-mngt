package com.mgnt.reservationservice.controller.dto.request;

import com.mgnt.reservationservice.domain.entity.Reservation;
import jakarta.validation.constraints.NotNull;

public record ReserveRequest(
        @NotNull Long concertId,
        @NotNull Long concertDateId,
        @NotNull int seatNum,
        @NotNull Long userId
) {

    public Reservation toEntity() { // 예약 엔티티를 만들기 위해서는 concertId, concertDateId, userId가 필요하다.
        return Reservation.builder()
                .concertId(concertId)  //
                .concertDateId(concertDateId)
                .seatNum(seatNum)
                .userId(userId)
                .status(Reservation.Status.ING)
                .build();
    }
}

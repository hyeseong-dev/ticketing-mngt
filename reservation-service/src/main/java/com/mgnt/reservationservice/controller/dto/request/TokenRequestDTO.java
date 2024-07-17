package com.mgnt.reservationservice.controller.dto.request;

public record TokenRequestDTO(
        Long concertId,
        Long concertDateId
) {
}

package com.mgnt.reservationservice.controller.dto.request;

import jakarta.validation.constraints.NotNull;

public record CancelRequest(
        @NotNull Long userId
) {
}

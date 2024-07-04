package com.mgnt.ticketing.controller.reservation.dto.request;

import jakarta.validation.constraints.NotNull;

public record CancelRequest(
        @NotNull Long userId
) {
}

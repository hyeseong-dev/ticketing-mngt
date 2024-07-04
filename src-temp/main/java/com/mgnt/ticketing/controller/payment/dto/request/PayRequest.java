package com.mgnt.ticketing.controller.payment.dto.request;

import jakarta.validation.constraints.NotNull;

public record PayRequest(
        @NotNull Long userId
) {
}

package com.mgnt.paymentservice.controller.dto.request;

import jakarta.validation.constraints.NotNull;

public record PayRequest(
        @NotNull Long userId
) {
}

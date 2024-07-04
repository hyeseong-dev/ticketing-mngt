package com.mgnt.userservice.controller.dto.request;

import jakarta.validation.constraints.Positive;

public record ChargeRequest(
        @Positive int amount
) {
}

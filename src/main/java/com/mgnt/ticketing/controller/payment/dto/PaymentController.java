package com.mgnt.ticketing.controller.payment.dto;

import com.mgnt.ticketing.controller.payment.dto.request.PayRequest;
import com.mgnt.ticketing.controller.payment.dto.response.PayResponse;
import com.mgnt.ticketing.domain.payment.PaymentEnums;
import com.mgnt.ticketing.domain.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "결제", description = "payment-controller")
@RequestMapping("/payments")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @Operation(summary = "결제 요청")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PayResponse.class)))
    @PostMapping("/{paymentId}")
    public PayResponse pay(@PathVariable(value = "paymentId") @NotNull Long paymentId,
                           @RequestBody @Valid PayRequest request) {
        // dummy data
        return PayResponse.builder()
                .paymentId(1L)
                .status(PaymentEnums.Status.COMPLETE)
                .paymentPrice(BigDecimal.valueOf(79000))
                .balance(BigDecimal.valueOf(1000))
                .build();
    }
}

package com.mgnt.ticketing.controller.payment.dto;

import com.mgnt.ticketing.base.exception.ApiResult;
import com.mgnt.ticketing.controller.payment.dto.request.CreateRequest;
import com.mgnt.ticketing.controller.payment.dto.request.PayRequest;
import com.mgnt.ticketing.controller.payment.dto.response.CreateResponse;
import com.mgnt.ticketing.controller.payment.dto.response.PayResponse;
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

@Tag(name = "결제", description = "payment-controller")
@RequestMapping("/payments")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @Operation(summary = "결제 생성")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PayResponse.class)))
    @PostMapping("")
    public ApiResult<CreateResponse> create(@RequestBody @Valid CreateRequest request) {
        return ApiResult.success(service.create(request));
    }

    @Operation(summary = "결제 요청")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PayResponse.class)))
    @PostMapping("/{paymentId}")
    public ApiResult<PayResponse> pay(@PathVariable(value = "paymentId") @NotNull Long paymentId,
                                      @RequestBody @Valid PayRequest request) {
        return ApiResult.success(service.pay(paymentId, request));
    }
}

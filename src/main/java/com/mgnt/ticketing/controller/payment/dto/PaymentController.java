package com.mgnt.ticketing.controller.payment.dto;

import com.mgnt.ticketing.controller.payment.dto.request.PayRequest;
import com.mgnt.ticketing.controller.payment.dto.response.PayResponse;
import com.mgnt.ticketing.domain.payment.PaymentEnums;
import com.mgnt.ticketing.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/payments")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @PostMapping("/{paymentId}")
    public PayResponse pay(@PathVariable(value = "paymentId") @NotNull Long paymentId,
                           @RequestBody @Valid PayRequest request) {
        // dummy data
        return PayResponse.builder()
                .paymentId(1L)
                .status(PaymentEnums.Status.COMPLETE)
                .paymentPrice(79000)
                .balance(1000)
                .build();
    }
}
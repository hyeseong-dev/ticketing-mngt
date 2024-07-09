package com.mgnt.paymentservice.controller;

import com.mgnt.core.exception.ApiResult;
import com.mgnt.paymentservice.controller.dto.request.CreateRequest;
import com.mgnt.paymentservice.controller.dto.request.PayRequest;
import com.mgnt.paymentservice.controller.dto.response.CreateResponse;
import com.mgnt.paymentservice.controller.dto.response.PayResponse;
import com.mgnt.paymentservice.domain.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/payments")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;


//    @PostMapping("")
//    public ApiResult<CreateResponse> create(@RequestBody @Valid CreateRequest request) {
//        return ApiResult.success(service.create(request));
//    }
//
//
//    @PostMapping("/{paymentId}")
//    public ApiResult<PayResponse> pay(@PathVariable(value = "paymentId") @NotNull Long paymentId,
//                                      @RequestBody @Valid PayRequest request) {
//        return ApiResult.success(service.pay(paymentId, request));
//    }
}

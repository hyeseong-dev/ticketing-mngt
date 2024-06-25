package com.mgnt.ticketing.domain.payment.service;

import com.mgnt.ticketing.controller.payment.dto.request.PayRequest;
import com.mgnt.ticketing.controller.payment.dto.response.PayResponse;

public interface PaymentInterface {

    // 결제 요청
    PayResponse pay(Long paymentId, PayRequest request);
}

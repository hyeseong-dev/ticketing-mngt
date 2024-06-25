package com.mgnt.ticketing.domain.payment.service;

import com.mgnt.ticketing.controller.payment.dto.request.PayRequest;
import com.mgnt.ticketing.controller.payment.dto.response.PayResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService implements PaymentInterface {

    @Override
    public PayResponse pay(Long paymentId, PayRequest request) {
        return null;
    }
}

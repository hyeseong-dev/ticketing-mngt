package com.mgnt.ticketing.domain.payment.service;

import com.mgnt.ticketing.controller.payment.dto.request.PayRequest;
import com.mgnt.ticketing.controller.payment.dto.response.PayResponse;
import com.mgnt.ticketing.domain.payment.PaymentEnums;
import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.payment.repository.PaymentRepository;
import com.mgnt.ticketing.domain.user.entity.User;
import com.mgnt.ticketing.domain.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService implements PaymentInterface {

    private final PaymentRepository paymentRepository;
    private final PaymentValidator paymentValidator;
    private final UserReader userReader;

    @Override
    public PayResponse pay(Long paymentId, PayRequest request) {
        // validator - 결제 상태 검증
        Payment payment = paymentRepository.findById(paymentId);
        paymentValidator.checkStatus(payment.getStatus());

        // validator - 사용자 잔액 검증
        User user = userReader.getUser(request.userId());
        paymentValidator.checkBalance(payment.getPrice(), user.getBalance());

        // 결제
        // 1. 결제
        boolean isSuccess = false;
        Payment paymentResult = payment.completePay();
        BigDecimal usedBalance = user.getBalance();
        if (paymentResult.getStatus().equals(PaymentEnums.Status.COMPLETE)) {
            // 2. 사용자 잔액 차감
            usedBalance = user.useBalance(payment.getPrice());
            isSuccess = true;
        }

        return PayResponse.from(isSuccess, paymentResult, usedBalance);
    }
}

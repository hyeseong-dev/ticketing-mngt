package com.mgnt.ticketing.domain.payment.service;

import com.mgnt.ticketing.base.exception.CustomException;
import com.mgnt.ticketing.domain.payment.PaymentEnums;
import com.mgnt.ticketing.domain.payment.PaymentExceptionEnum;
import com.mgnt.ticketing.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PaymentValidator {

    private final PaymentRepository paymentRepository;

    public void checkBalance(BigDecimal paymentPrice, BigDecimal balance) {
        if (balance.compareTo(paymentPrice) < 0) {
            throw new CustomException(PaymentExceptionEnum.INSUFFICIENT_BALANCE);
        }
    }

    public void checkStatus(PaymentEnums.Status status) {
        if (!status.equals(PaymentEnums.Status.READY)) {
            throw new CustomException(PaymentExceptionEnum.NOT_AVAILABLE_STATUS);
        }
    }
}

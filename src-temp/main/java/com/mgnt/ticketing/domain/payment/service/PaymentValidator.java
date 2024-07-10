package com.mgnt.ticketing.domain.payment.service;

import com.mgnt.ticketing.base.exception.CustomException;
import com.mgnt.ticketing.domain.payment.PaymentExceptionEnum;
import com.mgnt.ticketing.domain.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 결제 유효성 검사 클래스
 * <p>
 * 이 클래스는 결제와 관련된 유효성 검사를 처리합니다.
 */
@Component
@RequiredArgsConstructor
public class PaymentValidator {

    /**
     * 잔액 검증
     *
     * @param price   결제 금액
     * @param balance 사용자 잔액
     * @throws CustomException 잔액이 부족한 경우 예외 발생
     */
    public void checkBalance(BigDecimal price, BigDecimal balance) {
        if (balance.compareTo(price) < 0) {
            throw new CustomException(PaymentExceptionEnum.INSUFFICIENT_BALANCE, null, LogLevel.INFO);
        }
    }

    /**
     * 결제 상태 검증
     *
     * @param status 결제 상태
     * @throws CustomException 결제 가능한 상태가 아닌 경우 예외 발생
     */
    public void checkPayStatus(Payment.Status status) {
        if (!status.equals(Payment.Status.READY)) {
            throw new CustomException(PaymentExceptionEnum.NOT_AVAILABLE_PAY, null, LogLevel.INFO);
        }
    }

    /**
     * 결제 취소 상태 검증
     *
     * @param status 결제 상태
     * @throws CustomException 결제 취소 가능한 상태가 아닌 경우 예외 발생
     */
    public void checkCancelStatus(Payment.Status status) {
        if (!(status.equals(PaymentStatus.READY) || status.equals(PaymentStatus.COMPLETE))) {
            throw new CustomException(PaymentExceptionEnum.NOT_AVAILABLE_CANCEL, null, LogLevel.INFO);
        }
    }
}

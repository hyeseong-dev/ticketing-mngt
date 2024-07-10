package com.mgnt.paymentservice.domain.service;

import com.mgnt.core.enums.PaymentStatus;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.exception.CustomException;
import com.mgnt.paymentservice.domain.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
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
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE, null, Level.INFO);
        }
    }

    /**
     * 결제 상태 검증
     *
     * @param status 결제 상태
     * @throws CustomException 결제 가능한 상태가 아닌 경우 예외 발생
     */
    public void checkPayStatus(PaymentStatus status) {
        if (!status.equals(PaymentStatus.READY)) {
            throw new CustomException(ErrorCode.NOT_AVAILABLE_PAY, null, Level.INFO);
        }
    }

    /**
     * 결제 취소 상태 검증
     *
     * @param status 결제 상태
     * @throws CustomException 결제 취소 가능한 상태가 아닌 경우 예외 발생
     */
    public void checkCancelStatus(PaymentStatus status) {
        if (!(status.equals(PaymentStatus.READY) || status.equals(PaymentStatus.COMPLETE))) {
            throw new CustomException(ErrorCode.NOT_AVAILABLE_CANCEL, null, Level.INFO);
        }
    }
}

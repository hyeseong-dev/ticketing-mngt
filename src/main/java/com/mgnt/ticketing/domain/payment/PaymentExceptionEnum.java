package com.mgnt.ticketing.domain.payment;

import com.mgnt.ticketing.base.enums.MessageCommInterface;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 결제 예외 enum 클래스
 *
 * 이 클래스는 결제와 관련된 예외를 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum PaymentExceptionEnum implements MessageCommInterface {

    INSUFFICIENT_BALANCE("PAYMENT.INSUFFICIENT_BALANCE", "잔액이 부족합니다."),
    NOT_AVAILABLE_PAY("PAYMENT.NOT_AVAILABLE_PAY", "결제 가능한 상태가 아닙니다."),
    NOT_AVAILABLE_CANCEL("PAYMENT.NOT_AVAILABLE_CANCEL", "취소 가능한 상태가 아닙니다."),
    ;

    private final String code;
    private final String message;
}

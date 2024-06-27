package com.mgnt.ticketing.domain.payment;

import com.mgnt.ticketing.base.enums.MessageCommInterface;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentExceptionEnum implements MessageCommInterface {

    INSUFFICIENT_BALANCE("PAYMENT.INSUFFICIENT_BALANCE", "잔액이 부족합니다."),
    NOT_AVAILABLE_STATUS("PAYMENT.NOT_AVAILABLE_STATUS", "결제 가능한 상태가 아닙니다."),
    ;

    private final String code;
    private final String message;
}
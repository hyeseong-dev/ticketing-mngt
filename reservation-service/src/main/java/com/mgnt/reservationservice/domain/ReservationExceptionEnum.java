package com.mgnt.reservationservice.domain;

import com.mgnt.core.enums.MessageCommInterface;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationExceptionEnum implements MessageCommInterface {

    ALREADY_RESERVED("RESERVATION.ALREADY_RESERVED", "이미 선택된 좌석입니다."),
    IS_NULL("RESERVATION.IS_NULL", "예약 정보가 없습니다."),
    ;

    private final String code;
    private final String message;
}
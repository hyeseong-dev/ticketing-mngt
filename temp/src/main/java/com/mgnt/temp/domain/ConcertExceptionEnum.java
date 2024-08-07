package com.mgnt.temp.domain;

import com.mgnt.ticketing.base.enums.MessageCommInterface;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConcertExceptionEnum implements MessageCommInterface {

    DATE_IS_NULL("CONCERT.DATE_IS_NULL", "예정된 콘서트 날짜가 없습니다."),
    ;

    private final String code;
    private final String message;
}
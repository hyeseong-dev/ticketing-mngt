package com.mgnt.ticketing.base.exception;

import com.mgnt.ticketing.base.enums.MessageCommInterface;
import lombok.Getter;

/**
 * 커스텀 예외 클래스
 *
 * 이 클래스는 런타임 시 발생할 수 있는 커스텀 예외를 나타냅니다.
 */
public class CustomException extends RuntimeException {
    @Getter
    private final String errorCode;
    private final String message;

    /**
     * MessageCommInterface를 사용하여 CustomException 생성
     *
     * @param messageCommInterface 메시지 공통 인터페이스
     */
    public CustomException(MessageCommInterface messageCommInterface) {
        super(messageCommInterface.getMessage());
        this.errorCode = messageCommInterface.getCode();
        this.message = messageCommInterface.getMessage();
    }

    /**
     * 예외 메시지 반환
     *
     * @return 예외 메시지
     */
    @Override
    public String getMessage() {
        return message;
    }
}

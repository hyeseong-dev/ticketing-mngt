package com.mgnt.common.exception;

import com.mgnt.ticketing.base.enums.MessageCommInterface;
import lombok.Getter;
import org.springframework.boot.logging.LogLevel;

/**
 * 커스텀 예외 클래스
 * <p>
 * 이 클래스는 런타임 시 발생할 수 있는 커스텀 예외를 나타냅니다.
 */
@Getter
public class CustomException extends RuntimeException {
    private final String errorCode;
    private final String message;
    private final Object data;
    private final LogLevel logLevel;

    /**
     * MessageCommInterface를 사용하여 CustomException 생성
     *
     * @param messageCommInterface 메시지 공통 인터페이스
     * @param data                 예외와 관련된 추가 데이터
     * @param logLevel             로그 레벨
     */
    public CustomException(MessageCommInterface messageCommInterface, Object data, LogLevel logLevel) {
        super(messageCommInterface.getMessage());
        this.errorCode = messageCommInterface.getCode();
        this.message = messageCommInterface.getMessage();
        this.data = data;
        this.logLevel = logLevel;
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

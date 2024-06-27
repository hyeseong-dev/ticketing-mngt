package com.mgnt.ticketing.base.exception;

import com.mgnt.ticketing.base.enums.MessageCommInterface;

import java.io.Serializable;

/**
 * API 결과를 나타내는 클래스
 *
 * @param <T> 응답 데이터의 타입
 */
public record ApiResult<T> (
        boolean success,
        T data,
        Error error
) implements Serializable {

    /**
     * 내용이 없는 성공 응답을 생성합니다.
     *
     * @param <T> 응답 데이터의 타입
     * @return 성공 응답
     */
    public static <T> ApiResult<T> successNoContent() {
        return new ApiResult<>(true, null, null);
    }

    /**
     * 데이터를 포함한 성공 응답을 생성합니다.
     *
     * @param data 응답 데이터
     * @param <T> 응답 데이터의 타입
     * @return 성공 응답
     */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(true, data, null);
    }

    /**
     * 실패 응답을 생성합니다.
     *
     * @param messageCommInterface 실패 메시지 인터페이스
     * @param <T> 응답 데이터의 타입
     * @return 실패 응답
     */
    public static <T> ApiResult<T> fail(MessageCommInterface messageCommInterface) {
        return new ApiResult<>(false, null, new Error(messageCommInterface.getCode(), messageCommInterface.getMessage()));
    }

    /**
     * 오류 정보를 나타내는 클래스
     *
     * @param errorCode 오류 코드
     * @param message 오류 메시지
     */
    public record Error(
            String errorCode,
            String message
    ) {
    }
}

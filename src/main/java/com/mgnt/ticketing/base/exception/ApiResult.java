package com.mgnt.ticketing.base.exception;

import java.io.Serializable;

/**
 * API 결과를 나타내는 클래스
 *
 * @param <T> 응답 데이터의 타입
 */
public record ApiResult<T>(
        boolean success,
        T data,
        Error error
) implements Serializable {

    public static <T> ApiResult<T> successNoContent() {
        return new ApiResult<>(true, null, null);
    }

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(true, data, null);
    }

    public static <T> ApiResult<T> error(String errorCode, String message, T data) {
        return new ApiResult<>(false, data, new Error(errorCode, message));
    }

    public static <T> ApiResult<T> error(String message) {
        return new ApiResult<>(false, null, new Error(null, message));
    }

    public record Error(
            String errorCode,
            String message
    ) {
    }
}
package com.mgnt.ticketing.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * API 예외 처리를 위한 어드바이스 클래스
 *
 * 이 클래스는 컨트롤러 전역에서 발생하는 예외를 처리합니다.
 */
@RestControllerAdvice
@Slf4j
class ApiControllerAdvice {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 커스텀 예외를 처리합니다.
     *
     * @param e 커스텀 예외 객체
     * @return 에러 응답 엔티티
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResult<?>> handleCustomException(CustomException e) {
        switch (e.getLogLevel()) {
            case ERROR -> log.error("ApiException : {}", e.getMessage(), e);
            case WARN -> log.warn("ApiException : {}", e.getMessage(), e);
            default -> log.info("ApiException : {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(ApiResult.error(e.getErrorCode(), e.getMessage(), e.getData()), HttpStatus.OK);
    }

    /**
     * 일반 예외를 처리합니다.
     *
     * @param e 예외 객체
     * @return 에러 응답 엔티티
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<?>> handleException(Exception e) {
        log.error("UnhandledException : {}", e.getMessage(), e);
        return new ResponseEntity<>(ApiResult.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
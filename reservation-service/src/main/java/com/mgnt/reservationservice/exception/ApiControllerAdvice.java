package com.mgnt.reservationservice.exception;

import com.mgnt.core.exception.ApiResult;
import com.mgnt.core.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.apache.logging.log4j.Level.ERROR;
import static org.apache.logging.log4j.Level.WARN;


@RestControllerAdvice
@Slf4j
class ApiControllerAdvice {
    private final Logger logger = LoggerFactory.getLogger(getClass());


    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResult<?>> handleCustomException(CustomException e) {
        if (e.getLogLevel().equals(ERROR)) {
            log.error("ApiException : {}", e.getMessage(), e);
        } else if (e.getLogLevel().equals(WARN)) {
            log.warn("ApiException : {}", e.getMessage(), e);
        } else {
            log.info("ApiException : {}", e.getMessage(), e);
        }
        return new ResponseEntity<>(ApiResult.error(e.getErrorCode(), e.getMessage(), e.getData()), HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<?>> handleException(Exception e) {
        log.error("UnhandledException : {}", e.getMessage(), e);
        return new ResponseEntity<>(ApiResult.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
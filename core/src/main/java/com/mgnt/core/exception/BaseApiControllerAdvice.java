package com.mgnt.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.apache.logging.log4j.Level.ERROR;
import static org.apache.logging.log4j.Level.WARN;

public abstract class BaseApiControllerAdvice {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResult<?>> handleCustomException(CustomException e) {
        logException(e);
        return new ResponseEntity<>(ApiResult.error(e.getErrorCode(), e.getMessage(), e.getData()), HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<?>> handleException(Exception e) {
        logger.error("UnhandledException : {}", e.getMessage(), e);
        return new ResponseEntity<>(ApiResult.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected void logException(CustomException e) {
        if (e.getLogLevel().equals(ERROR)) {
            logger.error("ApiException : {}", e.getMessage(), e);
        } else if (e.getLogLevel().equals(WARN)) {
            logger.warn("ApiException : {}", e.getMessage(), e);
        } else {
            logger.info("ApiException : {}", e.getMessage(), e);
        }
    }
}
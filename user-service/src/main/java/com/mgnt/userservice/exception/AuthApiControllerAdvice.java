package com.mgnt.userservice.exception;

import com.mgnt.core.exception.ApiResult;
import com.mgnt.core.exception.BaseApiControllerAdvice;
import com.mgnt.core.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthApiControllerAdvice extends BaseApiControllerAdvice {
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
}

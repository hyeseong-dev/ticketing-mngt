package com.mgnt.ticketing.base.error.exceptions;

import com.mgnt.ticketing.base.error.ErrorCode;
import lombok.Getter;

@Getter
public class CustomAccessDeniedException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomAccessDeniedException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

package com.mgnt.core.error.exceptions;

import com.mgnt.core.error.ErrorCode;
import lombok.Getter;

@Getter
public class CustomAccessDeniedException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomAccessDeniedException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

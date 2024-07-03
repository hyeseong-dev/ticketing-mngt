package com.mgnt.common.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {
    OK(200, "S000", "SUCCESS");

    private final int status;
    private final String code;
    private final String message;
}

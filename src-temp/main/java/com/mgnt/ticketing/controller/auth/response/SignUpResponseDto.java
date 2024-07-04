package com.mgnt.ticketing.controller.auth.response;

import com.mgnt.ticketing.base.error.ErrorCode;
import com.mgnt.ticketing.base.dto.response.SuccessCode;
import lombok.Getter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@AllArgsConstructor
public class SignUpResponseDto {
    private String code;
    private String message;

    public static ResponseEntity<SignUpResponseDto> success() {
        SignUpResponseDto result = new SignUpResponseDto(SuccessCode.OK.getCode(), SuccessCode.OK.getMessage());
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<SignUpResponseDto> duplicatedEmail() {
        SignUpResponseDto result = new SignUpResponseDto(ErrorCode.EMAIL_ALREADY_EXISTS.getCode(), ErrorCode.EMAIL_ALREADY_EXISTS.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    public static ResponseEntity<SignUpResponseDto> failure(ErrorCode errorCode) {
        SignUpResponseDto result = new SignUpResponseDto(errorCode.getCode(), errorCode.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    public static ResponseEntity<SignUpResponseDto> failure(String code, String message) {
        SignUpResponseDto result = new SignUpResponseDto(code, message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}

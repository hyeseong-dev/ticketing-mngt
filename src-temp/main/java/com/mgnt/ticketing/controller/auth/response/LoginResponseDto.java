package com.mgnt.ticketing.controller.auth.response;

import com.mgnt.ticketing.base.error.ErrorCode;
import com.mgnt.ticketing.base.dto.response.SuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@AllArgsConstructor
public class LoginResponseDto {
    private String code;
    private String message;
    private String accessToken;
    private String refreshToken;

    public LoginResponseDto(String code, String message) {
        this.code = code;
        this.message = message;
    }


    public static ResponseEntity<LoginResponseDto> success(String accessToken, String refreshToken) {
        LoginResponseDto result = new LoginResponseDto(SuccessCode.OK.getCode(), SuccessCode.OK.getMessage(), accessToken, refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<LoginResponseDto> failure(String code, String message) {
        LoginResponseDto result = new LoginResponseDto(code, message, null, null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }
    public static ResponseEntity<LoginResponseDto> failure(ErrorCode errorCode) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new LoginResponseDto(
                        errorCode.getCode(),
                        errorCode.getMessage())
                );
    }
}

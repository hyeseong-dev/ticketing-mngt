package com.mgnt.ticketing.dto.response.auth;

import com.mgnt.ticketing.dto.response.SuccessCode;
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

    public static ResponseEntity<LoginResponseDto> success(String accessToken, String refreshToken) {
        LoginResponseDto result = new LoginResponseDto(SuccessCode.OK.getCode(), SuccessCode.OK.getMessage(), accessToken, refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<LoginResponseDto> failure(String code, String message) {
        LoginResponseDto result = new LoginResponseDto(code, message, null, null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }
}

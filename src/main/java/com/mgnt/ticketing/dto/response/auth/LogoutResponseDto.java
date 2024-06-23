package com.mgnt.ticketing.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@AllArgsConstructor
public class LogoutResponseDto {
    private String code;
    private String message;

    public static ResponseEntity<LogoutResponseDto> success() {
        LogoutResponseDto result = new LogoutResponseDto("S000", "Logout successful.");
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<LogoutResponseDto> failure(String code, String message) {
        LogoutResponseDto result = new LogoutResponseDto(code, message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}

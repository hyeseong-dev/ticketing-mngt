package com.mgnt.ticketing.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshResponseDto {
    private String code;
    private String message;
    private String accessToken;
    private String refreshToken;

    public static ResponseEntity<RefreshResponseDto> success(String accessToken, String refreshToken) {
        RefreshResponseDto result = new RefreshResponseDto("S000", "Token refresh successful.", accessToken, refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<RefreshResponseDto> failure(String code, String message) {
        RefreshResponseDto result = new RefreshResponseDto(code, message, null, null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}

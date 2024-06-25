package com.mgnt.ticketing.controller.auth.dto.response;

import com.mgnt.ticketing.base.error.ErrorCode;
import com.mgnt.ticketing.base.dto.ResponseDto;
import com.mgnt.ticketing.base.dto.response.SuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshResponseDto extends ResponseDto {
    private String code;
    private String message;
    private String accessToken;
    private String refreshToken;

    public RefreshResponseDto(String code, String message) {
        this(code, message, null, null);
    }

    public static ResponseEntity<RefreshResponseDto> success(String accessToken, String refreshToken) {
        RefreshResponseDto result = new RefreshResponseDto(SuccessCode.OK.getCode(),
                                                            SuccessCode.OK.getMessage(),
                                                            accessToken,
                                                            refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }


    public static ResponseEntity<RefreshResponseDto> failure(String code, String message) {
        RefreshResponseDto result = new RefreshResponseDto(code, message, null, null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
    public static ResponseEntity<RefreshResponseDto> failure(ErrorCode errorCode) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new RefreshResponseDto(errorCode.getCode(),errorCode.getMessage()));
    }

}

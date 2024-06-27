package com.mgnt.ticketing.controller.auth.response;

import com.mgnt.ticketing.base.error.ErrorCode;
import com.mgnt.ticketing.base.dto.ResponseDto;
import com.mgnt.ticketing.base.dto.response.SuccessCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@NoArgsConstructor
public class EmailResponseDto extends ResponseDto {

    private EmailResponseDto(String code, String message) {
        super(code, message);
    }

    public static ResponseEntity<EmailResponseDto> success() {
        return ResponseEntity.status(HttpStatus.OK).body(
                new EmailResponseDto(SuccessCode.OK.getCode(),
                                    SuccessCode.OK.getMessage()));
    }

    public static ResponseEntity<EmailResponseDto> failure() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new EmailResponseDto(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                                     ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }

    public static ResponseEntity<EmailResponseDto> failure(String code, String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new EmailResponseDto(code, message));
    }

    public static ResponseEntity<EmailResponseDto> failure(ErrorCode errorCode) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new EmailResponseDto(errorCode.getCode(),
                        errorCode.getMessage()));
    }
}

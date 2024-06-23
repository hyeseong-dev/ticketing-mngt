package com.mgnt.ticketing.dto.response.user;

import com.mgnt.ticketing.common.error.ErrorCode;
import com.mgnt.ticketing.dto.response.SuccessCode;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@NoArgsConstructor
public class AdminModifyResponseDto {
    private UserResponseDto user;
    private String code;
    private String message;

    @Builder
    public AdminModifyResponseDto(UserResponseDto user, String code, String message) {
        this.user = user;
        this.code = code;
        this.message = message;
    }

    public static ResponseEntity<AdminModifyResponseDto> success(UserResponseDto user) {
        AdminModifyResponseDto response = AdminModifyResponseDto.builder()
                .user(user)
                .code(SuccessCode.OK.getCode())
                .message(SuccessCode.OK.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public static ResponseEntity<AdminModifyResponseDto> failure(ErrorCode errorCode) {
        AdminModifyResponseDto response = AdminModifyResponseDto.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}

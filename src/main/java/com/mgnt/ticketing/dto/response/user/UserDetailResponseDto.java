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
public class UserDetailResponseDto {
    private UserResponseDto user;
    private String code;
    private String message;

    @Builder
    public UserDetailResponseDto(UserResponseDto user, String code, String message) {
        this.user = user;
        this.code = code;
        this.message = message;
    }

    public static ResponseEntity<UserDetailResponseDto> success(UserResponseDto user) {
        UserDetailResponseDto response = UserDetailResponseDto.builder()
                .user(user)
                .code(SuccessCode.OK.getCode())
                .message(SuccessCode.OK.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public static ResponseEntity<UserDetailResponseDto> failure(ErrorCode errorCode) {
        UserDetailResponseDto response = UserDetailResponseDto.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}

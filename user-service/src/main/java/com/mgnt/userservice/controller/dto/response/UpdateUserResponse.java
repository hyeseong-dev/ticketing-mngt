package com.mgnt.userservice.controller.dto.response;

import com.mgnt.core.dto.response.SuccessCode;
import com.mgnt.core.error.ErrorCode;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@NoArgsConstructor
public class UpdateUserResponse {
    private UserResponseDto data;
    private String code;
    private String message;

    @Builder
    public UpdateUserResponse(UserResponseDto user, String code, String message) {
        this.data = user;
        this.code = code;
        this.message = message;
    }

    public static ResponseEntity<UpdateUserResponse> success(UserResponseDto user) {
        UpdateUserResponse response = UpdateUserResponse.builder()
                .user(user)
                .code(SuccessCode.OK.getCode())
                .message(SuccessCode.OK.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public static ResponseEntity<UpdateUserResponse> failure(ErrorCode errorCode) {
        UpdateUserResponse response = UpdateUserResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}

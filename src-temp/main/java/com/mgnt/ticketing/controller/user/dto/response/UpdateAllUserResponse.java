package com.mgnt.ticketing.controller.user.dto.response;

import com.mgnt.ticketing.base.error.ErrorCode;
import com.mgnt.ticketing.base.dto.response.SuccessCode;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@NoArgsConstructor
public class UpdateAllUserResponse {
    private UserResponseDto user;
    private String code;
    private String message;

    @Builder
    public UpdateAllUserResponse(UserResponseDto user, String code, String message) {
        this.user = user;
        this.code = code;
        this.message = message;
    }

    public static ResponseEntity<UpdateAllUserResponse> success(UserResponseDto user) {
        UpdateAllUserResponse response = UpdateAllUserResponse.builder()
                .user(user)
                .code(SuccessCode.OK.getCode())
                .message(SuccessCode.OK.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public static ResponseEntity<UpdateAllUserResponse> failure(ErrorCode errorCode) {
        UpdateAllUserResponse response = UpdateAllUserResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}

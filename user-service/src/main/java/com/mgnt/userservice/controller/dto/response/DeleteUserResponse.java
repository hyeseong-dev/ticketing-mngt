package com.mgnt.userservice.controller.dto.response;

import com.mgnt.core.dto.response.SuccessCode;
import com.mgnt.core.error.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@NoArgsConstructor
public class DeleteUserResponse {
    private String code;
    private String message;

    public static ResponseEntity<DeleteUserResponse> success(UserResponseDto from) {
        DeleteUserResponse response = new DeleteUserResponse();
        response.code = SuccessCode.OK.getCode();
        response.message = SuccessCode.OK.getMessage();
        ;
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public static ResponseEntity<DeleteUserResponse> failure(ErrorCode errorCode) {
        DeleteUserResponse response = new DeleteUserResponse();
        response.code = errorCode.getCode();
        response.message = errorCode.getMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}

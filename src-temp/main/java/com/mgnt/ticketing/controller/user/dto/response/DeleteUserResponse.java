package com.mgnt.ticketing.controller.user.dto.response;

import com.mgnt.ticketing.base.error.ErrorCode;
import com.mgnt.ticketing.base.dto.response.SuccessCode;
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
        response.message = SuccessCode.OK.getMessage();;
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public static ResponseEntity<DeleteUserResponse> failure(ErrorCode errorCode) {
        DeleteUserResponse response = new DeleteUserResponse();
        response.code = errorCode.getCode();
        response.message = errorCode.getMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}

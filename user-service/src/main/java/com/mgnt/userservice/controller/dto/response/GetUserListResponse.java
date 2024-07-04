package com.mgnt.userservice.controller.dto.response;

import com.mgnt.core.dto.response.SuccessCode;
import com.mgnt.core.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetUserListResponse {
    private List<UserResponseDto> data;
    private String code;
    private String message;

    public static ResponseEntity<GetUserListResponse> success(List<UserResponseDto> users) {
        GetUserListResponse response = GetUserListResponse.builder()
                .data(users)
                .code(SuccessCode.OK.getCode())
                .message(SuccessCode.OK.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public static ResponseEntity<GetUserListResponse> failure(ErrorCode errorCode) {
        GetUserListResponse response = GetUserListResponse.builder()
                .data(List.of())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}

package com.mgnt.ticketing.dto.response.user;

import com.mgnt.ticketing.common.error.ErrorCode;
import com.mgnt.ticketing.dto.response.SuccessCode;
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
public class UserListResponseDto {
    private List<UserResponseDto> userList;
    private String code;
    private String message;

    public static ResponseEntity<UserListResponseDto> success(List<UserResponseDto> users) {
        UserListResponseDto response = UserListResponseDto.builder()
                .userList(users)
                .code(SuccessCode.OK.getCode())
                .message(SuccessCode.OK.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public static ResponseEntity<UserListResponseDto> failure(ErrorCode errorCode) {
        UserListResponseDto response = UserListResponseDto.builder()
                .userList(List.of())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}

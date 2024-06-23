package com.mgnt.ticketing.dto.response.user;

import com.mgnt.ticketing.common.error.ErrorCode;
import com.mgnt.ticketing.dto.response.SuccessCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@NoArgsConstructor
public class UserDeleteResponseDto {
    private String code;
    private String message;

    public static ResponseEntity<UserDeleteResponseDto> success(UserResponseDto from) {
        UserDeleteResponseDto response = new UserDeleteResponseDto();
        response.code = SuccessCode.OK.getCode();
        response.message = SuccessCode.OK.getMessage();;
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public static ResponseEntity<UserDeleteResponseDto> failure(ErrorCode errorCode) {
        UserDeleteResponseDto response = new UserDeleteResponseDto();
        response.code = errorCode.getCode();
        response.message = errorCode.getMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}

package com.mgnt.ticketing.dto.response.auth;

import com.mgnt.ticketing.dto.ResponseDto;
import com.mgnt.ticketing.dto.response.ResponseCode;
import com.mgnt.ticketing.dto.response.ResponseMessage;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@AllArgsConstructor
public class LoginResponseDto extends ResponseDto {

    private String accessToken;
    private String refreshToken;

    private LoginResponseDto (String code, String message, String accessToken, String refreshToken) {
        super(code, message);
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static ResponseEntity<LoginResponseDto> success(String accessToken, String refreshToken) {
        LoginResponseDto result = new LoginResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS, accessToken, refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<ResponseDto> failure() {
        ResponseDto result = new ResponseDto(ResponseCode.LOGIN_FAILED, ResponseMessage.LOGIN_FAILED);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }

}

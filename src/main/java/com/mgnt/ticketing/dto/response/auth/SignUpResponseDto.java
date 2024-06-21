package com.mgnt.ticketing.dto.response.auth;

import com.mgnt.ticketing.dto.ResponseDto;
import com.mgnt.ticketing.dto.response.ResponseCode;
import com.mgnt.ticketing.dto.response.ResponseMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@NoArgsConstructor
@Builder
public class SignUpResponseDto extends ResponseDto {

    private String code;
    private String message;

    private SignUpResponseDto(String code, String message) {
        super(code, message);
        this.code = code;
        this.message = message;
    }

    public static ResponseEntity<SignUpResponseDto> success() {
        SignUpResponseDto result = SignUpResponseDto.builder()
                .code(ResponseCode.SUCCESS)
                .message(ResponseMessage.SUCCESS)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public static ResponseEntity<SignUpResponseDto> duplicateEmail() {
        SignUpResponseDto result = SignUpResponseDto.builder()
                .code(ResponseCode.DUPLICATED_EMAIL)
                .message(ResponseMessage.DUPLICATED_EMAIL)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
}

package com.mgnt.ticketing.controller.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequestDto {

    @NotBlank
    @Email(message = "이메일 형식이 아닙니다.")
    private String email;

    @NotBlank
    @Length(min = 8, max = 20, message = "비밀번호는 여덟자 이상 스무자이하 이어야 합니다.")
    private String password;
}
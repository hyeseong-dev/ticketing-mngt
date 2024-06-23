package com.mgnt.ticketing.dto.request.auth;

import com.mgnt.ticketing.entity.UserRoleEnum;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDto {

    @NotBlank
    @Email(message = "이메일 형식이 아닙니다.")
    private String email;

    @NotBlank
    @Size(min=8, max=20, message = "비밀번호는 여덟자 이상 스무자이하 이어야 합니다.")
    private String password;

    @NotBlank
    @Pattern(regexp = "^[가-힣]{1,20}$", message = "이름은 한글로 한 글자 이상 스무자 이하 이어야 합니다.")
    private String name;

    @NotNull
    @Pattern(regexp = "USER|ADMIN", message = "Role은 'USER' 또는 'ADMIN'만 가능합니다.")
    private UserRoleEnum role;

}
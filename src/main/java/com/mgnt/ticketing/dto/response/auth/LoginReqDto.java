package com.mgnt.ticketing.dto.response.auth;

import com.mgnt.ticketing.entity.UserRoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginReqDto {

    @NotBlank
    private String name;

    @NotBlank @Size(min=8, max=20)
    private String password;

    @NotBlank @Email
    private String email;

    @NotBlank
    private UserRoleEnum role;

}

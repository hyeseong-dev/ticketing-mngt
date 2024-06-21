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
    @Email
    private String email;

    @NotBlank @Size(min=8, max=20)
    private String password;

    @NotBlank
    private String name;

    @NotNull
    private UserRoleEnum role;

}
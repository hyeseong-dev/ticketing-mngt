package com.mgnt.ticketing.controller.user.dto.request;

import com.mgnt.ticketing.base.constant.UserRoleEnum;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminModifyRequestDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min=8, max=20)
    private String password;

    @NotBlank
    private String name;

    @NotNull
    private Double balance;

    @NotNull
    private Boolean emailVerified;

    @NotNull
    private UserRoleEnum role;
}

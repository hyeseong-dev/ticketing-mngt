package com.mgnt.userservice.controller.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import lombok.Setter;

@Getter
@Setter
public class UserModifyRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 20)
    private String password;

    @Positive
    private Double balance;

    @NotBlank
    private String name;
}


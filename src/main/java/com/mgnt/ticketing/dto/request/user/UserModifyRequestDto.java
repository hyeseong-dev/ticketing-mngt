package com.mgnt.ticketing.dto.request.user;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class UserModifyRequestDto {
    @NotBlank @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 20)
    private String password;

    @Positive
    private Integer points;

    @NotBlank
    private String name;
}


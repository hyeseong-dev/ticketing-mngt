package com.mgnt.userservice.controller.dto.request;

import com.mgnt.userservice.domain.entity.UserRoleEnum;

public record SignupRequestDto(
        String email,
        String password,
        String name,
        UserRoleEnum role,
        String phoneNumber,
        String address
) {
}
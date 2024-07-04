package com.mgnt.userservice.controller.dto.response;

import com.mgnt.core.constant.UserRoleEnum;
import com.mgnt.userservice.domain.entity.Users;

import java.math.BigDecimal;


public record GetUserResponse(
        Long id,
        String email,
        String name,
        String phoneNumber,
        String address,
        BigDecimal balance,
        Boolean emailVerified,
        UserRoleEnum role
) {
    public static GetUserResponse from(Users user) {
        return new GetUserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.getAddress(),
                user.getBalance(),
                user.getEmailVerified(),
                user.getRole()
        );
    }

}

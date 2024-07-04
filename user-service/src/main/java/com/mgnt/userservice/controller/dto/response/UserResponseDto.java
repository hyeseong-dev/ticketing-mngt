package com.mgnt.userservice.controller.dto.response;

import com.mgnt.userservice.domain.entity.UserRoleEnum;
import com.mgnt.userservice.domain.entity.Users;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;
    private String name;
    private BigDecimal balance;
    private Boolean emailVerified;
    private UserRoleEnum role;
    private String phoneNumber;
    private String address;

    @Builder
    public UserResponseDto(Long id,
                           String email,
                           String name,
                           String phoneNumber,
                           String address,
                           BigDecimal balance,
                           Boolean emailVerified,
                           UserRoleEnum role
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.balance = balance;
        this.emailVerified = emailVerified;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }


    public static UserResponseDto from(Users user) {
        return UserResponseDto.builder()
                .id(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .balance(user.getBalance())
                .emailVerified(user.getEmailVerified())
                .role(user.getRole())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .build();
    }

}

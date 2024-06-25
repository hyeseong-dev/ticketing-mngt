package com.mgnt.ticketing.controller.user.dto.response;

import com.mgnt.ticketing.domain.user.entity.User;
import com.mgnt.ticketing.base.constant.UserRoleEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;
    private String name;
    private Double balance;
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
                           Double balance,
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


    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
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

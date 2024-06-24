package com.mgnt.ticketing.dto.response.user;

import com.mgnt.ticketing.entity.UserEntity;
import com.mgnt.ticketing.entity.UserRoleEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;
    private String name;
    private Integer points;
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
                           Integer points,
                           Boolean emailVerified,
                           UserRoleEnum role
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.points = points;
        this.emailVerified = emailVerified;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }


    public static UserResponseDto from(UserEntity user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .points(user.getPoints())
                .emailVerified(user.getEmailVerified())
                .role(user.getRole())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .build();
    }

}

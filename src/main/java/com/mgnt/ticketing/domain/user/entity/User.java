package com.mgnt.ticketing.domain.user.entity;

import com.mgnt.ticketing.base.entity.BaseDateTimeEntity;
import com.mgnt.ticketing.controller.auth.dto.request.SignUpRequestDto;
import com.mgnt.ticketing.base.constant.UserRoleEnum;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)  // 수정: id -> user_id
    private Long userId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ColumnDefault("0")
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    @ColumnDefault("0")
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRoleEnum role;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "address", nullable = false)
    private String address;

    @Builder
    public User(Long userId,
                String email,
                String password,
                String name,
                BigDecimal balance,
                ZonedDateTime deletedAt,
                Boolean emailVerified,
                UserRoleEnum role,
                String phoneNumber,
                String address) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.name = name;
        this.balance = balance != null ? balance : BigDecimal.valueOf(0);
        this.emailVerified = emailVerified != null ? emailVerified : false;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public static User from(SignUpRequestDto dto, String password) {
        return User.builder()
                .email(dto.getEmail())
                .name(dto.getName())
                .password(password)
                .phoneNumber(dto.getPhoneNumber())
                .address(dto.getAddress())
                .role(dto.getRole())
                .balance(BigDecimal.valueOf(0))
                .emailVerified(false)
                .build();
    }

    public BigDecimal useBalance(BigDecimal price) {
        this.balance = balance.subtract(price);
        return this.balance;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }

    public void updateUserInfo(String name, String phoneNumber, String address) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public void updateBalance(BigDecimal newBalance) {
        this.balance = newBalance;
    }

    public void updateRole(UserRoleEnum role) {
        this.role = role;
    }

    public void updateDeletedAt(ZonedDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public void updateName(String name){
        this.name = name;
    }
}

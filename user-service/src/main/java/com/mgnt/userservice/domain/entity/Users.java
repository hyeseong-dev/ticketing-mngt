package com.mgnt.userservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users extends BaseDateTimeEntity {

    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
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

    @ColumnDefault("FALSE")
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRoleEnum role;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "address", nullable = false, unique = true)
    private String address;

    public Users(Long userId, BigDecimal balance) {
        this.userId = userId;
        this.balance = balance;
    }

    @Builder
    public Users(Long userId,
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

    public BigDecimal useBalance(BigDecimal price) {
        this.balance = balance.subtract(price);
        return this.balance;
    }

    public Users chargeBalance(BigDecimal amount) {
        this.balance = balance.add(amount);
        return this;
    }

    public void refundBalance(BigDecimal price) {
        this.balance = balance.add(price);
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

    public void updateName(String name) {
        this.name = name;
    }
}

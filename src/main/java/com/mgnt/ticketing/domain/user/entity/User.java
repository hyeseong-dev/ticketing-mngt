package com.mgnt.ticketing.domain.user.entity;

import com.mgnt.ticketing.base.entity.BaseDateTimeEntity;
import com.mgnt.ticketing.controller.auth.request.SignUpRequestDto;
import com.mgnt.ticketing.base.constant.UserRoleEnum;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * 사용자 엔티티 클래스
 *
 * 이 클래스는 사용자 정보를 나타내며, 데이터베이스의 'users' 테이블과 매핑됩니다.
 */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseDateTimeEntity {

    @Id
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

    /**
     * 생성자
     *
     * @param userId 사용자 ID
     * @param email 이메일
     * @param password 비밀번호
     * @param name 이름
     * @param balance 잔액
     * @param deletedAt 삭제된 시간
     * @param emailVerified 이메일 인증 여부
     * @param role 사용자 역할
     * @param phoneNumber 전화번호
     * @param address 주소
     */
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

    /**
     * 회원가입 요청 DTO로부터 사용자 엔티티 생성
     *
     * @param dto 회원가입 요청 DTO
     * @param password 암호화된 비밀번호
     * @return 생성된 사용자 엔티티
     */
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

    /**
     * 잔액 사용
     *
     * @param price 사용 금액
     * @return 사용 후 잔액
     */
    public BigDecimal useBalance(BigDecimal price) {
        this.balance = balance.subtract(price);
        return this.balance;
    }

    /**
     * 잔액 충전
     *
     * @param amount 충전 금액
     * @return 잔액이 충전된 사용자 엔티티
     */
    public User chargeBalance(BigDecimal amount) {
        this.balance = balance.add(amount);
        return this;
    }

    /**
     * 잔액 환불
     *
     * @param price 환불 금액
     */
    public void refundBalance(BigDecimal price) {
        this.balance = balance.add(price);
    }

    /**
     * 이메일 인증 상태 설정
     *
     * @param emailVerified 이메일 인증 여부
     */
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    /**
     * 비밀번호 업데이트
     *
     * @param newPassword 새 비밀번호
     */
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    /**
     * 이메일 업데이트
     *
     * @param newEmail 새 이메일
     */
    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }

    /**
     * 사용자 정보 업데이트
     *
     * @param name 새 이름
     * @param phoneNumber 새 전화번호
     * @param address 새 주소
     */
    public void updateUserInfo(String name, String phoneNumber, String address) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    /**
     * 잔액 업데이트
     *
     * @param newBalance 새 잔액
     */
    public void updateBalance(BigDecimal newBalance) {
        this.balance = newBalance;
    }

    /**
     * 사용자 역할 업데이트
     *
     * @param role 새 역할
     */
    public void updateRole(UserRoleEnum role) {
        this.role = role;
    }

    /**
     * 삭제된 시간 업데이트
     *
     * @param deletedAt 삭제된 시간
     */
    public void updateDeletedAt(ZonedDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    /**
     * 이름 업데이트
     *
     * @param name 새 이름
     */
    public void updateName(String name) {
        this.name = name;
    }
}

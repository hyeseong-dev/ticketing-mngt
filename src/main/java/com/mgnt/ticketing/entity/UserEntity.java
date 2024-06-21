package com.mgnt.ticketing.entity;

import com.mgnt.ticketing.dto.request.auth.SignUpRequestDto;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
@NoArgsConstructor
public class UserEntity extends AuditingFields{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ColumnDefault("0")
    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ColumnDefault("0")
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRoleEnum role;


    @Builder
    public UserEntity(Long id, String email, String password, String name, Integer points, LocalDateTime deletedAt, Boolean emailVerified, UserRoleEnum role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.points = points != null ? points : 0;
        this.emailVerified = emailVerified != null ? emailVerified : false;
        this.role = role;
    }

    // 회원가입용 정적 팩토리 메서드
    public static UserEntity from(SignUpRequestDto dto, String password) {
        return UserEntity.builder()
                .email(dto.getEmail())
                .name(dto.getName())
                .password(password)
                .role(dto.getRole())
                .points(0) // 초기 포인트 설정
                .emailVerified(false) // 초기 이메일 인증 상태 설정
                .build();
    }

    // 이메일 인증 상태를 업데이트하는 메서드
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
}

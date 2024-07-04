package com.mgnt.ticketing.domain.auth.entity;

import com.mgnt.ticketing.domain.user.entity.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "refresh_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 외부에서 무분별한 인스턴스 생성을 막기 위해 PROTECTED 접근 지정자 사용
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "ip", nullable = false, length = 50)
    private String ip;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "expiry_date", nullable = false, updatable = false)
    private LocalDateTime expiryDate;

    @Builder
    public RefreshToken(Users user, String token, String ip, String deviceInfo, LocalDateTime expiryDate) {
        this.user = user;
        this.token = token;
        this.ip = ip;
        this.deviceInfo = deviceInfo;
        this.expiryDate = expiryDate;
    }

    public RefreshToken(Users user, String refreshToken) {
        this.user = user;
        this.token = refreshToken;
    }

    public void updateRefreshToken(String token) {
        this.token = token;
    }

    public boolean validateRefreshToken(String refreshToken) {
        return this.token == refreshToken;
    }
}

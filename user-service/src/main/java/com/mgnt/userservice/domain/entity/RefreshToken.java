package com.mgnt.userservice.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 외부에서 무분별한 인스턴스 생성을 막기 위해 PROTECTED 접근 지정자 사용
public class RefreshToken {


    private Long id;

    private Long user_id;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "ip", nullable = false, length = 50)
    private String ip;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "expiry_date", nullable = false, updatable = false)
    private LocalDateTime expiryDate;

    @Builder
    public RefreshToken(Long user_id, String token, String ip, String deviceInfo, LocalDateTime expiryDate) {
        this.user_id = user_id;
        this.token = token;
        this.ip = ip;
        this.deviceInfo = deviceInfo;
        this.expiryDate = expiryDate;
    }

    public RefreshToken(Long user_id, String refreshToken) {
        this.user_id = user_id;
        this.token = refreshToken;
    }

    public void updateRefreshToken(String token) {
        this.token = token;
    }

    public boolean validateRefreshToken(String refreshToken) {
        return this.token == refreshToken;
    }
}

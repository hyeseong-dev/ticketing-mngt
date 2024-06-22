package com.mgnt.ticketing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity extends AuditingFields{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "ip", nullable = false, length = 50)
    private String ip;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "expiry_date", nullable = false, updatable = false)
    private LocalDateTime expiryDate;

}
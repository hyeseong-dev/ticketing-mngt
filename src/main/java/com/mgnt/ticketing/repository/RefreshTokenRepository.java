package com.mgnt.ticketing.repository;

import com.mgnt.ticketing.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser_Email(String email);
    void deleteByUser_Email(String email);
}

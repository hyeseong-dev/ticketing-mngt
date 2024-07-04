package com.mgnt.ticketing.domain.auth.repository;

import com.mgnt.ticketing.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser_Email(String email);
    void deleteByUser_Email(String email);
}

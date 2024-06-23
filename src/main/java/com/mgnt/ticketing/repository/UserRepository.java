package com.mgnt.ticketing.repository;

import com.mgnt.ticketing.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    List<UserEntity> findAllByDeletedAtNull();
    Optional<UserEntity> findByIdAndDeletedAtNull(Long id);
}

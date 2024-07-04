package com.mgnt.ticketing.domain.user.repository;

import com.mgnt.ticketing.domain.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Users> findAllByDeletedAtNull();

    Optional<Users> findByUserIdAndDeletedAtNull(Long id);

    boolean existsByPhoneNumber(String phoneNumber);
}

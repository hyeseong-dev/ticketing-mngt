package com.mgnt.ticketing.repository;

import com.mgnt.ticketing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAllByDeletedAtNull();
    Optional<User> findByIdAndDeletedAtNull(Long id);
    boolean existsByPhoneNumber(String phoneNumber);
}

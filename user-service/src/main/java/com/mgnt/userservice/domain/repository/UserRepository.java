package com.mgnt.userservice.domain.repository;

import com.mgnt.userservice.domain.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long>, UserRepositoryCustom {

    boolean existsByEmail(String email);

    Optional<Users> findByEmail(String email);

    Optional<Users> findById(Long userId);

    List<Users> findAllByDeletedAtNull();

    Optional<Users> findByUserIdAndDeletedAtNull(Long id);

    boolean existsByPhoneNumber(String phoneNumber);
}
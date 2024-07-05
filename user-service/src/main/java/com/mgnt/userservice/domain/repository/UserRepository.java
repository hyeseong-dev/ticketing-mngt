package com.mgnt.userservice.domain.repository;

import com.mgnt.userservice.domain.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Users> findAllByDeletedAtNull();

    Optional<Users> findByUserIdAndDeletedAtNull(Long id);

    boolean existsByPhoneNumber(String phoneNumber);

 
}

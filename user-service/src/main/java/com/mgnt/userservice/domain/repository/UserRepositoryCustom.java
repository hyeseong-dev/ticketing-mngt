package com.mgnt.userservice.domain.repository;

import com.mgnt.userservice.domain.entity.Users;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryCustom {
    Optional<Users> findUserByEmail(String email);

    List<Users> findUsersWithNoDeletion();

    Optional<Users> findUserByIdIfNotDeleted(Long id);
}
package com.mgnt.userservice.domain.repository;

import com.mgnt.userservice.domain.entity.Users;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {
    Users findById(Long userId);

    void save(Users user);

}

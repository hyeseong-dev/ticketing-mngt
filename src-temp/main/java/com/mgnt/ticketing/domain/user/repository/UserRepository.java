package com.mgnt.ticketing.domain.user.repository;

import com.mgnt.ticketing.domain.user.entity.Users;
import com.mgnt.ticketing.domain.user.entity.Users;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {
    Users findById(Long userId);

    void save(Users user);

}

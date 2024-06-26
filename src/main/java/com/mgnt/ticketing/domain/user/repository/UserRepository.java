package com.mgnt.ticketing.domain.user.repository;

import com.mgnt.ticketing.domain.user.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {
    User findById(Long userId);

}

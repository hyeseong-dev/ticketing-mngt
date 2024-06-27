package com.mgnt.ticketing.infra_structure;

import com.mgnt.ticketing.domain.user.entity.User;
import com.mgnt.ticketing.domain.user.repository.UserJpaRepository;
import com.mgnt.ticketing.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryImpl(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User findById(Long userId) {
        return userJpaRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public void save(User user) {
        userJpaRepository.save(user);
    }
}

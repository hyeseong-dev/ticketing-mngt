package com.mgnt.ticketing.infra_structure;

import com.mgnt.ticketing.domain.user.repository.UserJpaRepository;
import com.mgnt.ticketing.domain.user.repository.UserRepository;

public class UserRepositoryImpl implements UserRepository {

    private UserJpaRepository userJpaRepository;
}

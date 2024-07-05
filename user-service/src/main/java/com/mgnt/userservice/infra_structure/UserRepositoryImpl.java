//package com.mgnt.userservice.infra_structure;
//
//import com.mgnt.userservice.domain.entity.Users;
//import com.mgnt.userservice.domain.repository.UserJpaRepository;
//import com.mgnt.userservice.domain.repository.UserRepository;
//import jakarta.persistence.EntityNotFoundException;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public class UserRepositoryImpl implements UserRepository {
//
//    private final UserJpaRepository userJpaRepository;
//
//    public UserRepositoryImpl(UserJpaRepository userJpaRepository) {
//        this.userJpaRepository = userJpaRepository;
//    }
//
//    @Override
//    public Users findById(Long userId) {
//        return userJpaRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
//    }
//
//    @Override
//    public void save(Users user) {
//        userJpaRepository.save(user);
//    }
//}
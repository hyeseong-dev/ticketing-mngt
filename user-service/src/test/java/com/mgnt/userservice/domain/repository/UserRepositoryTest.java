package com.mgnt.userservice.domain.repository;

import com.mgnt.userservice.domain.entity.UserRoleEnum;
import com.mgnt.userservice.domain.entity.Users;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Import(UserRepositoryTest.PasswordEncoderConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional
    void createAndRetrieveRandomNumberOfUsers() {
        int batchSize = 100;
        int numberOfUsers = 2000;

        for (int i = 0; i < numberOfUsers; i++) {
            String phoneNumber = String.format("010-0000-%04d", i);
            Users user = Users.builder()
                    .email("user" + i + "@example.com")
                    .password(passwordEncoder.encode("12345678"))
                    .name("User " + i)
                    .balance(BigDecimal.valueOf(10000))
                    .emailVerified(true)
                    .role(UserRoleEnum.USER)
                    .phoneNumber(phoneNumber)
                    .address("Address " + i)
                    .build();

            entityManager.persist(user);

            if (i % batchSize == 0 && i > 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.flush();
        entityManager.clear();

        // 저장된 사용자 수 확인
        assertEquals(numberOfUsers, userRepository.count());

        // 추가적인 검증 로직...
    }

    @TestConfiguration
    static class PasswordEncoderConfig {
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
}
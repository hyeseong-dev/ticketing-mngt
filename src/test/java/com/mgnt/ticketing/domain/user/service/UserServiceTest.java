package com.mgnt.ticketing.domain.user.service;

import com.mgnt.ticketing.controller.user.dto.request.ChargeRequest;
import com.mgnt.ticketing.controller.user.dto.response.GetBalanceResponse;
import com.mgnt.ticketing.domain.user.entity.User;
import com.mgnt.ticketing.domain.user.repository.UserJpaRepository;
import com.mgnt.ticketing.domain.user.repository.UserRepository;
import com.mgnt.ticketing.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.when;

class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;
    private UserJpaRepository userJpaRepository;
    private PasswordEncoder passwordEncoder;
    private User 사용자;

    @BeforeEach
    void setUp() {
        // mocking
        userRepository = Mockito.mock(UserRepository.class);
        userJpaRepository = Mockito.mock(UserJpaRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class) ;
        userService = new UserService(
                userRepository,
                userJpaRepository,
                passwordEncoder
        );

        // 사용자 정보 세팅
        사용자 = new User(1L, BigDecimal.valueOf(100000));
    }

    @Test
    @DisplayName("잔액_조회")
    void getBalanceTest_잔액_조회() {
        // given
        Long userId = 1L;

        // when
        when(userRepository.findById(userId)).thenReturn(사용자);
        GetBalanceResponse response = userService.getBalance(userId);

        // then
        assertThat(response.balance()).isEqualTo(BigDecimal.valueOf(100000));
    }

    @Test
    @DisplayName("잔액_충전")
    void chargeTest_잔액_충전() {
        // given
        Long userId = 1L;
        ChargeRequest request = new ChargeRequest(10000);

        // when
        when(userRepository.findById(userId)).thenReturn(사용자);
        GetBalanceResponse response = userService.charge(userId, request);

        // then
        assertThat(response.balance()).isEqualTo(BigDecimal.valueOf(110000));
    }
}
package com.mgnt.ticketing.service;

import com.mgnt.ticketing.dto.request.auth.SignUpRequestDto;
import com.mgnt.ticketing.dto.response.ResponseMessage;
import com.mgnt.ticketing.dto.response.auth.SignUpResponseDto;
import com.mgnt.ticketing.entity.UserEntity;
import com.mgnt.ticketing.entity.UserRoleEnum;
import com.mgnt.ticketing.repository.UserRepository;
import com.mgnt.ticketing.service.implement.AuthServiceImplement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthServiceImplement authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;


    @Test
    @DisplayName("Given 유효한 회원가입 정보가 주어졌을 때, When 회원가입을 시도하면, Then 성공적으로 회원가입이 완료된다.")
    void givenValidSignUpInfo_WhenSignUp_ThenSignUpSuccessfully() {
        SignUpRequestDto dto = new SignUpRequestDto("test@example.com", "password", "Test User", UserRoleEnum.USER);

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<? super SignUpResponseDto> response = authService.signUp(dto);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("SU", ((SignUpResponseDto) response.getBody()).getCode());
        assertEquals(ResponseMessage.SIGN_UP_SUCCESS, ((SignUpResponseDto) response.getBody()).getMessage());
    }

    @Test
    @DisplayName("Given 중복된 이메일이 주어졌을 때, When 회원가입을 시도하면, Then 중복된 이메일로 인해 회원가입이 실패한다.")
    void givenDuplicateEmail_WhenSignUp_ThenFailDueToDuplicateEmail() {
        SignUpRequestDto dto = new SignUpRequestDto("duplicate@example.com", "password", "Test User", UserRoleEnum.USER);

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        ResponseEntity<? super SignUpResponseDto> response = authService.signUp(dto);

        assertEquals(409, response.getStatusCodeValue());
        assertEquals("DE", ((SignUpResponseDto) response.getBody()).getCode());
        assertEquals(ResponseMessage.DUPLICATED_EMAIL, ((SignUpResponseDto) response.getBody()).getMessage());
    }

    @Test
    @DisplayName("Given 회원가입 중 예외가 발생할 때, When 회원가입을 시도하면, Then 데이터베이스 오류로 인해 회원가입이 실패한다.")
    void givenExceptionDuringSignUp_WhenSignUp_ThenSignUpFails() {
        SignUpRequestDto dto = new SignUpRequestDto("error@example.com", "password", "Test User", UserRoleEnum.USER);

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
        doThrow(new RuntimeException("Database error")).when(userRepository).save(any(UserEntity.class));

        ResponseEntity<? super SignUpResponseDto> response = authService.signUp(dto);

        assertEquals(500, response.getStatusCodeValue());
        assertEquals("DBE", ((SignUpResponseDto) response.getBody()).getCode());
        assertEquals(ResponseMessage.DATABASE_ERROR, ((SignUpResponseDto) response.getBody()).getMessage());
    }
}

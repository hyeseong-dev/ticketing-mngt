package com.mgnt.userservice.domain.service;

import com.mgnt.userservice.controller.dto.request.LoginRequestDto;
import com.mgnt.userservice.controller.dto.request.SignupRequestDto;
import com.mgnt.userservice.controller.dto.response.LoginResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface AuthInterface {

    Void signup(SignupRequestDto dto);

    LoginResponseDto login(LoginRequestDto dto, HttpServletRequest request);

    void logout(String accessToken);

//    RefreshResponseDto refresh(String refreshToken, HttpServletRequest request);
}
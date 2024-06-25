package com.mgnt.ticketing.domain.auth.service;


import com.mgnt.ticketing.controller.auth.dto.request.LoginRequestDto;
import com.mgnt.ticketing.controller.auth.dto.request.SignUpRequestDto;
import com.mgnt.ticketing.controller.auth.dto.response.LoginResponseDto;
import com.mgnt.ticketing.controller.auth.dto.response.LogoutResponseDto;
import com.mgnt.ticketing.controller.auth.dto.response.RefreshResponseDto;
import com.mgnt.ticketing.controller.auth.dto.response.SignUpResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface AuthInterface {

    ResponseEntity<? super SignUpResponseDto> signUp(SignUpRequestDto dto);
    ResponseEntity<? super LoginResponseDto> login(LoginRequestDto dto, HttpServletRequest request);
    ResponseEntity<? super LogoutResponseDto> logout(String accessToken);
    ResponseEntity<? super RefreshResponseDto> refresh(String accessToken, HttpServletRequest request);
}
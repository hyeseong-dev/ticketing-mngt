package com.mgnt.ticketing.service;


import com.mgnt.ticketing.dto.ResponseDto;
import com.mgnt.ticketing.dto.request.auth.LoginRequestDto;
import com.mgnt.ticketing.dto.request.auth.SignUpRequestDto;
import com.mgnt.ticketing.dto.response.auth.LoginResponseDto;
import com.mgnt.ticketing.dto.response.auth.LogoutResponseDto;
import com.mgnt.ticketing.dto.response.auth.RefreshResponseDto;
import com.mgnt.ticketing.dto.response.auth.SignUpResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity<? super SignUpResponseDto> signUp(SignUpRequestDto dto);
    ResponseEntity<? super LoginResponseDto> login(LoginRequestDto dto, HttpServletRequest request);
    ResponseEntity<? super LogoutResponseDto> logout(String accessToken);
    ResponseEntity<? super RefreshResponseDto> refresh(String accessToken, HttpServletRequest request);
}
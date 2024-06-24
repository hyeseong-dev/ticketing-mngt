package com.mgnt.ticketing.controller;

import com.mgnt.ticketing.dto.request.auth.LoginRequestDto;
import com.mgnt.ticketing.dto.request.auth.SignUpRequestDto;
import com.mgnt.ticketing.dto.response.auth.LoginResponseDto;
import com.mgnt.ticketing.dto.response.auth.LogoutResponseDto;
import com.mgnt.ticketing.dto.response.auth.RefreshResponseDto;
import com.mgnt.ticketing.dto.response.auth.SignUpResponseDto;
import com.mgnt.ticketing.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<? super SignUpResponseDto> signUp(
            @RequestBody @Valid SignUpRequestDto requestBody) {
        return authService.signUp(requestBody);
    }

    @PostMapping("/login")
    public ResponseEntity<? super LoginResponseDto> login(
            @RequestBody @Valid LoginRequestDto requestBody, HttpServletRequest request) {
        return authService.login(requestBody, request);
    }

    // 로그아웃 코드를 만들어야한다.
    @GetMapping("/logout")
    public ResponseEntity<? super LogoutResponseDto> logout(@RequestHeader("Authorization") String accessToken) {
        return authService.logout(accessToken);
    }
//     refresh API를 다른 signup, login코드와 일관성을 갖도록 코드 스타일로 작성해야 한다.
    @GetMapping("/refresh")
    public ResponseEntity<? super RefreshResponseDto> refresh(@RequestHeader("Authorization") String accessToken, HttpServletRequest request) {
        return authService.refresh(accessToken, request);
    }
}



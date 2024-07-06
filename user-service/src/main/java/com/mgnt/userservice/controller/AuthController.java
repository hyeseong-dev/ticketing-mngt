package com.mgnt.userservice.controller;

import com.mgnt.core.exception.ApiResult;
import com.mgnt.userservice.controller.dto.request.EmailVerificationRequestDto;
import com.mgnt.userservice.controller.dto.request.LoginRequestDto;
import com.mgnt.userservice.controller.dto.request.SignupRequestDto;
import com.mgnt.userservice.controller.dto.response.LoginResponseDto;
import com.mgnt.userservice.controller.dto.response.RefreshTokenResponseDto;
import com.mgnt.userservice.domain.service.AuthService;
import com.mgnt.userservice.domain.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
//    private final EmailService emailService;


    @PostMapping("/signup")
    public ApiResult<Void> signup(
            @RequestBody SignupRequestDto request
    ) throws Exception {
        log.info("Received SignupRequestDto: {}", request);
        authService.signup(request);
        return ApiResult.success(null);
    }

    @PostMapping("/verify-email")
    public ApiResult<Boolean> verifyEmail(@RequestBody EmailVerificationRequestDto request) {
        boolean isVerified = emailService.verifyEmail(request.email(), request.code());
        return ApiResult.success(isVerified);
    }

    @PostMapping("/login")
    public ApiResult<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        log.info("Received LoginRequestDto: {}", request);
        return ApiResult.success(authService.login(request));
    }


    @GetMapping("/logout")
    public ApiResult<Void> logout(
            @RequestHeader("User-Id") String userId,
            @RequestHeader("Access-Token") String accessToken
    ) {
        authService.logout(userId, accessToken);
        return ApiResult.success(null);
    }


    @GetMapping("/refresh")
    public ApiResult<RefreshTokenResponseDto> refresh(
            @RequestHeader("User-Id") String userId,
            @RequestHeader("Refresh-Token") String refreshToken
    ) {
        return ApiResult.success(authService.refresh(userId, refreshToken));
    }


}

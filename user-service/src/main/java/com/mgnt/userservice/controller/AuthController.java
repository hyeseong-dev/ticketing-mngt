package com.mgnt.userservice.controller;

import com.mgnt.core.exception.ApiResult;
import com.mgnt.userservice.controller.dto.request.EmailVerificationRequestDto;
import com.mgnt.userservice.controller.dto.request.SignupRequestDto;
import com.mgnt.userservice.domain.service.AuthService;
import com.mgnt.userservice.domain.service.EmailInterface;
import com.mgnt.userservice.domain.service.EmailService;
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

//    @PostMapping("/login")
//    public ApiResult<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
//        log.info("Received LoginRequestDto: {}", request);
//        LoginResponseDto response = authInterface.login(request.mapToCommand());
//        return ApiResult.success(response);
//    }
//
//
//    @GetMapping("/logout")
//    public ResponseEntity<? super LogoutResponseDto> logout(@RequestHeader("Authorization") String accessToken) {
//        return authInterface.logout(accessToken);
//    }
//
//
//    @GetMapping("/refresh")
//    public ResponseEntity<? super RefreshResponseDto> refresh(@RequestHeader("ReAuthroization") String refreshToken, HttpServletRequest request) {
//        return authInterface.refresh(refreshToken, request);
//    }
//
//

}

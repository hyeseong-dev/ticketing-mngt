package com.mgnt.ticketing.controller.auth;

import com.mgnt.ticketing.controller.auth.dto.request.LoginRequestDto;
import com.mgnt.ticketing.controller.auth.dto.request.SignUpRequestDto;
import com.mgnt.ticketing.controller.auth.dto.response.LoginResponseDto;
import com.mgnt.ticketing.controller.auth.dto.response.LogoutResponseDto;
import com.mgnt.ticketing.controller.auth.dto.response.RefreshResponseDto;
import com.mgnt.ticketing.controller.auth.dto.response.SignUpResponseDto;
import com.mgnt.ticketing.domain.auth.service.AuthInterface;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthInterface authInterface;

    @PostMapping("/signup")
    public ResponseEntity<? super SignUpResponseDto> signUp(
            @RequestBody @Valid SignUpRequestDto requestBody) {
        return authInterface.signUp(requestBody);
    }

    @PostMapping("/login")
    public ResponseEntity<? super LoginResponseDto> login(
            @RequestBody @Valid LoginRequestDto requestBody, HttpServletRequest request) {
        return authInterface.login(requestBody, request);
    }

    // 로그아웃 코드를 만들어야한다.
    @GetMapping("/logout")
    public ResponseEntity<? super LogoutResponseDto> logout(@RequestHeader("Authorization") String accessToken) {
        return authInterface.logout(accessToken);
    }
//     refresh API를 다른 signup, login코드와 일관성을 갖도록 코드 스타일로 작성해야 한다.
    @GetMapping("/refresh")
    public ResponseEntity<? super RefreshResponseDto> refresh(@RequestHeader("Authorization") String accessToken, HttpServletRequest request) {
        return authInterface.refresh(accessToken, request);
    }
}



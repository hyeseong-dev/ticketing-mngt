package com.mgnt.ticketing.controller;

import com.mgnt.ticketing.dto.request.auth.SignUpRequestDto;
import com.mgnt.ticketing.dto.response.auth.SignUpResponseDto;
import com.mgnt.ticketing.dto.response.auth.TokenReqRes;
import com.mgnt.ticketing.service.AuthService;
import com.mgnt.ticketing.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

//    @PostMapping("/signup")
//    public ResponseEntity<TokenReqRes> signUp(@RequestBody TokenReqRes request) {
//        return ResponseEntity.ok(authService.signUp(request));
//    }
    @PostMapping("/signup")
    public ResponseEntity<? super SignUpResponseDto> signUp(
            @RequestBody @Valid SignUpRequestDto requestBody) {

        ResponseEntity<? super SignUpResponseDto> response = authService.signUp(requestBody);
        return response;
    }

//    @PostMapping("/login")
//    public ResponseEntity<TokenReqRes> login(@RequestBody TokenReqRes request) {
//        return ResponseEntity.ok(authService.login(request));
//    }
//
//    @PostMapping("/refresh")
//    public ResponseEntity<TokenReqRes> createRefreshToken(@RequestBody TokenReqRes request) {
//        return ResponseEntity.ok(authService.refreshToken(request));
//    }
}



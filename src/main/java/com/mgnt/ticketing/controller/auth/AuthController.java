package com.mgnt.ticketing.controller.auth;

import com.mgnt.ticketing.controller.auth.request.LoginRequestDto;
import com.mgnt.ticketing.controller.auth.request.SignUpRequestDto;
import com.mgnt.ticketing.controller.auth.response.EmailResponseDto;
import com.mgnt.ticketing.controller.auth.response.LoginResponseDto;
import com.mgnt.ticketing.controller.auth.response.LogoutResponseDto;
import com.mgnt.ticketing.controller.auth.response.RefreshResponseDto;
import com.mgnt.ticketing.domain.auth.service.AuthInterface;
import com.mgnt.ticketing.domain.auth.service.EmailInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "Authentication Controller")
public class AuthController {

    private final AuthInterface authInterface;
    private final EmailInterface emailInterface;

    @Operation(summary = "회원 가입", description = "회원 가입을 위한 API입니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SignUpRequestDto.class)))
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(
            @RequestBody @Valid SignUpRequestDto requestBody) {
        log.info("Received SignUpRequestDto: {}", requestBody);
        return authInterface.signUp(requestBody);
    }

    @Operation(summary = "로그인", description = "로그인을 위한 API입니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoginResponseDto.class)))
    @PostMapping("/login")
    public ResponseEntity<? super LoginResponseDto> login(
            @RequestBody @Valid LoginRequestDto requestBody, HttpServletRequest request) {
        return authInterface.login(requestBody, request);
    }

    @Operation(summary = "로그아웃", description = "로그아웃을 위한 API입니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LogoutResponseDto.class)))
    @GetMapping("/logout")
    public ResponseEntity<? super LogoutResponseDto> logout(@RequestHeader("Authorization") String accessToken) {
        return authInterface.logout(accessToken);
    }

    @Operation(summary = "토큰 갱신", description = "토큰 갱신을 위한 API입니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = RefreshResponseDto.class)))
    @GetMapping("/refresh")
    public ResponseEntity<? super RefreshResponseDto> refresh(@RequestHeader("ReAuthroization") String refreshToken, HttpServletRequest request) {
        return authInterface.refresh(refreshToken, request);
    }

    @Operation(summary = "계정 활성화(via 이메일)", description = "회원 가입이후 계정 활성화 처리를 위한 이메일 인증 API입니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = EmailResponseDto.class)))
    @GetMapping("/email")
    public ResponseEntity<? super EmailResponseDto> verifyEmail(@RequestParam(value = "token") String token) {
        return emailInterface.verifyEmail(token);
    }
}

package com.mgnt.ticketing.domain.auth.service;

import com.mgnt.ticketing.base.error.ErrorCode;
import com.mgnt.ticketing.base.error.exceptions.EmailSendException;
import com.mgnt.ticketing.base.event.AuthRegisteredEvent;
import com.mgnt.ticketing.base.jwt.JwtUtil;
import com.mgnt.ticketing.base.jwt.UserDetailsImpl;
import com.mgnt.ticketing.controller.auth.request.LoginRequestDto;
import com.mgnt.ticketing.controller.auth.request.SignUpRequestDto;
import com.mgnt.ticketing.controller.auth.response.LoginResponseDto;
import com.mgnt.ticketing.controller.auth.response.LogoutResponseDto;
import com.mgnt.ticketing.controller.auth.response.RefreshResponseDto;
import com.mgnt.ticketing.controller.auth.response.SignUpResponseDto;
import com.mgnt.ticketing.domain.auth.entity.RefreshToken;
import com.mgnt.ticketing.domain.auth.repository.RefreshTokenJpaRepository;
import com.mgnt.ticketing.domain.user.entity.User;
import com.mgnt.ticketing.domain.user.repository.UserJpaRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthInterface {

    private final UserJpaRepository userJpaRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailInterface emailInterface;
    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Transactional
    @Override
    public ResponseEntity<SignUpResponseDto> signUp(SignUpRequestDto dto) {
        try {
            boolean hasEmail = userJpaRepository.existsByEmail(dto.getEmail());
            if (hasEmail) return SignUpResponseDto.failure(ErrorCode.EMAIL_DUPLICATED);

            boolean hasPhoneNumber = userJpaRepository.existsByPhoneNumber(dto.getPhoneNumber());
            if (hasPhoneNumber) return SignUpResponseDto.failure(ErrorCode.PHONE_NUMBER_DUPLICATED);

            User user = User.from(dto, passwordEncoder.encode(dto.getPassword()));
            userJpaRepository.save(user);

            eventPublisher.publishEvent(new AuthRegisteredEvent(user.getEmail(), user.getName()));
//            emailService.sendVerificationEmail(userEntity.getEmail(), userEntity.getName());

            return SignUpResponseDto.success();
        } catch (EmailSendException e) {
            log.error("Email send error: {}", e.getMessage(), e);
            return SignUpResponseDto.failure(ErrorCode.EMAIL_SEND_ERROR);
        } catch (Exception e) {
            log.error("Database error: {}", e.getMessage(), e);
            return SignUpResponseDto.failure(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<LoginResponseDto> login(LoginRequestDto dto, HttpServletRequest request) {
        try {
            // 사용자 정보 로드
            UserDetails userDetails = userJpaRepository.findByEmail(dto.getEmail())
                    .map(UserDetailsImpl::new)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + dto.getEmail()));

            User user = ((UserDetailsImpl) userDetails).getUser();
            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword()))
                return LoginResponseDto.failure(ErrorCode.LOGIN_FAILED);

            // 이메일 인증 상태 확인
            if (!user.getEmailVerified()) return LoginResponseDto.failure(ErrorCode.UNVERIFED_ACCOUNT);

            // 이메일과 비밀번호를 사용하여 인증 시도
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );
//            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String accessToken = jwtUtil.createAccessToken(dto.getEmail());
            String refreshToken;

            Optional<RefreshToken> existingTokenOpt = refreshTokenJpaRepository.findByUser_Email(dto.getEmail());
            if (existingTokenOpt.isPresent()) {
                RefreshToken existingToken = existingTokenOpt.get();
                existingToken.setExpiryDate(LocalDateTime.now().plusDays(7));
                refreshToken = existingToken.getToken();
                refreshTokenJpaRepository.save(existingToken);
            } else {
                refreshToken = jwtUtil.createRefreshToken(dto.getEmail());
//                refreshToken = jwtUtil.generateRefreshToken(new HashMap<>(), userDetails);
                RefreshToken refreshTokenEntity = RefreshToken.builder()
                        .user(userJpaRepository.findByEmail(dto.getEmail()).get())
                        .token(refreshToken)
                        .ip(request.getRemoteAddr())
                        .deviceInfo(request.getHeader("User-Agent"))
                        .expiryDate(LocalDateTime.now().plusDays(7))
                        .build();
                refreshTokenJpaRepository.save(refreshTokenEntity);
            }

            return LoginResponseDto.success(accessToken, refreshToken);
        } catch (Exception e) {
            log.error("로그인 중 오류 발생: {}", e.getMessage());
            return LoginResponseDto.failure(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<LogoutResponseDto> logout(String accessToken) {
        try {
            if (accessToken == null || !accessToken.startsWith("Bearer ")) {
                return LogoutResponseDto.failure(ErrorCode.INVALID_INPUT_VALUE.getCode(), ErrorCode.INVALID_INPUT_VALUE.getMessage());
            }

            String token = accessToken.substring(7);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return LogoutResponseDto.failure(ErrorCode.ACCESS_DENIED.getCode(), ErrorCode.ACCESS_DENIED.getMessage());
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            try {
                jwtUtil.getEmailFromToken(token); // 만료된 토큰일 경우 예외 발생
                if (!jwtUtil.validateToken(token)) {
                    return LogoutResponseDto.failure(ErrorCode.INVALID_TYPE_VALUE.getCode(), ErrorCode.INVALID_TYPE_VALUE.getMessage());
                }
            } catch (ExpiredJwtException e) {
                log.warn("Expired token received during logout: {}", e.getMessage());
                return LogoutResponseDto.failure(ErrorCode.ACCESS_DENIED.getCode(), ErrorCode.ACCESS_DENIED.getMessage());
            }

            SecurityContextHolder.clearContext();
            refreshTokenJpaRepository.deleteByUser_Email(userDetails.getUsername());

            return LogoutResponseDto.success();
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생: {}", e.getMessage());
            return LogoutResponseDto.failure(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        }
    }


    @Override
    @Transactional
    public ResponseEntity<? super RefreshResponseDto> refresh(String accessToken, HttpServletRequest request) {
        try {
            if (accessToken == null || !accessToken.startsWith("Bearer "))
                return RefreshResponseDto.failure(ErrorCode.BAD_REQUEST);


            final String token = accessToken.substring(7);
            String userEmail;

            try {
                userEmail = jwtUtil.getEmailFromToken(token);
            } catch (ExpiredJwtException e) {
                userEmail = e.getClaims().getSubject(); // 만료된 토큰에서 이메일을 추출
            }

            if (userEmail == null) return RefreshResponseDto.failure(ErrorCode.USER_UNAUTHORIZED);

            Optional<RefreshToken> refreshTokenOpt = refreshTokenJpaRepository.findByUser_Email(userEmail);
            if (refreshTokenOpt.isEmpty()) return RefreshResponseDto.failure(ErrorCode.INVALID_INPUT_VALUE);


            UserDetails userDetails = userJpaRepository.findByEmail(userEmail)
                    .map(UserDetailsImpl::new)
                    .orElseThrow(() -> new UsernameNotFoundException(ErrorCode.INVALID_CREDENTIALS.getMessage()));

            String newAccessToken = jwtUtil.createAccessToken(userDetails.getUsername());
            String newRefreshToken = jwtUtil.createRefreshToken(userDetails.getUsername());

            refreshTokenJpaRepository.deleteByUser_Email(userEmail);

            RefreshToken refreshToken = RefreshToken.builder()
                    .user(userJpaRepository.findByEmail(userEmail).get())
                    .token(newRefreshToken)
                    .ip(request.getRemoteAddr())
                    .deviceInfo(request.getHeader("User-Agent"))
                    .expiryDate(LocalDateTime.now().plusDays(7))
                    .build();
            refreshTokenJpaRepository.save(refreshToken);

            return RefreshResponseDto.success(newAccessToken, newRefreshToken);

        } catch (Exception e) {
            log.error("Error during refresh token: {}", e.getMessage());
            return RefreshResponseDto.failure(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

}

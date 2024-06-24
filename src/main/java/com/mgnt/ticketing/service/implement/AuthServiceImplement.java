package com.mgnt.ticketing.service.implement;

import com.mgnt.ticketing.common.error.ErrorCode;
import com.mgnt.ticketing.dto.request.auth.LoginRequestDto;
import com.mgnt.ticketing.dto.request.auth.SignUpRequestDto;
import com.mgnt.ticketing.dto.response.auth.LoginResponseDto;
import com.mgnt.ticketing.dto.response.auth.LogoutResponseDto;
import com.mgnt.ticketing.dto.response.auth.RefreshResponseDto;
import com.mgnt.ticketing.dto.response.auth.SignUpResponseDto;
import com.mgnt.ticketing.entity.RefreshTokenEntity;
import com.mgnt.ticketing.entity.UserEntity;
import com.mgnt.ticketing.event.UserRegisteredEvent;
import com.mgnt.ticketing.repository.RefreshTokenRepository;
import com.mgnt.ticketing.repository.UserRepository;
import com.mgnt.ticketing.security.JwtUtil;
import com.mgnt.ticketing.security.UserDetailsImpl;
import com.mgnt.ticketing.service.AuthService;
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
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImplement implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ResponseEntity<SignUpResponseDto> signUp(SignUpRequestDto dto) {
        try {
            //  이메일 중복 여부
            boolean hasEmail = userRepository.existsByEmail(dto.getEmail());
            if (hasEmail) return SignUpResponseDto.failure(ErrorCode.EMAIL_DUPLICATED);

            // 휴대폰 중복 여부 검증
            boolean hasPhoneNumber = userRepository.existsByPhoneNumber(dto.getPhoneNumber());
            if (hasPhoneNumber) return SignUpResponseDto.failure(ErrorCode.PHONE_NUMBER_DUPLICATED);

            UserEntity userEntity = UserEntity.from(dto, passwordEncoder.encode(dto.getPassword()));
            userRepository.save(userEntity);

            // 이벤트 발행
            eventPublisher.publishEvent(new UserRegisteredEvent(userEntity.getEmail(), userEntity.getName()));

            return SignUpResponseDto.success();
        } catch (Exception exception) {
            log.error("Database error: {}", exception.getMessage(), exception);
            return SignUpResponseDto.failure(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<LoginResponseDto> login(LoginRequestDto dto, HttpServletRequest request) {
        try {
            // 사용자 정보 로드
            UserDetails userDetails = userRepository.findByEmail(dto.getEmail())
                    .map(UserDetailsImpl::new)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + dto.getEmail()));

            UserEntity userEntity = ((UserDetailsImpl) userDetails).getUser();
            if (!passwordEncoder.matches(dto.getPassword(), userEntity.getPassword()))
                return LoginResponseDto.failure(ErrorCode.LOGIN_FAILED);

            // 이메일 인증 상태 확인
            if (!userEntity.getEmailVerified()) return LoginResponseDto.failure(ErrorCode.UNVERIFED_ACCOUNT);

            // 이메일과 비밀번호를 사용하여 인증 시도
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );
            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String refreshToken;

            Optional<RefreshTokenEntity> existingTokenOpt = refreshTokenRepository.findByUser_Email(dto.getEmail());
            if (existingTokenOpt.isPresent()) {
                RefreshTokenEntity existingToken = existingTokenOpt.get();
                existingToken.setExpiryDate(LocalDateTime.now().plusDays(7));
                refreshToken = existingToken.getToken();
                refreshTokenRepository.save(existingToken);
            } else {
                refreshToken = jwtUtil.generateRefreshToken(new HashMap<>(), userDetails);
                RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
                refreshTokenEntity.setUser(userRepository.findByEmail(dto.getEmail()).get());
                refreshTokenEntity.setToken(refreshToken);
                refreshTokenEntity.setIp(request.getRemoteAddr());
                refreshTokenEntity.setDeviceInfo(request.getHeader("User-Agent"));
                refreshTokenEntity.setExpiryDate(LocalDateTime.now().plusDays(7));
                refreshTokenRepository.save(refreshTokenEntity);
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
            if (accessToken == null || !accessToken.startsWith(JwtUtil.BEARER_PREFIX)) {
                return LogoutResponseDto.failure(ErrorCode.INVALID_INPUT_VALUE.getCode(), ErrorCode.INVALID_INPUT_VALUE.getMessage());
            }

            String token = accessToken.substring(JwtUtil.BEARER_PREFIX.length());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return LogoutResponseDto.failure(ErrorCode.ACCESS_DENIED.getCode(), ErrorCode.ACCESS_DENIED.getMessage());
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            try {
                jwtUtil.extractUsername(token); // 만료된 토큰일 경우 예외 발생
                if (!jwtUtil.isTokenValid(token, userDetails)) {
                    return LogoutResponseDto.failure(ErrorCode.INVALID_TYPE_VALUE.getCode(), ErrorCode.INVALID_TYPE_VALUE.getMessage());
                }
            } catch (ExpiredJwtException e) {
                log.warn("Expired token received during logout: {}", e.getMessage());
                return LogoutResponseDto.failure(ErrorCode.ACCESS_DENIED.getCode(), ErrorCode.ACCESS_DENIED.getMessage());
            }

            SecurityContextHolder.clearContext();
            refreshTokenRepository.deleteByUser_Email(userDetails.getUsername());

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
            if (accessToken == null || !accessToken.startsWith(JwtUtil.BEARER_PREFIX))
                return RefreshResponseDto.failure(ErrorCode.BAD_REQUEST);


            final String token = accessToken.substring(JwtUtil.BEARER_PREFIX.length());
            String userEmail;

            try {
                userEmail = jwtUtil.extractUsername(token);
            } catch (ExpiredJwtException e) {
                userEmail = e.getClaims().getSubject(); // 만료된 토큰에서 이메일을 추출
            }

            if (userEmail == null) return RefreshResponseDto.failure(ErrorCode.USER_UNAUTHORIZED);

            Optional<RefreshTokenEntity> refreshTokenOpt = refreshTokenRepository.findByUser_Email(userEmail);
            if (refreshTokenOpt.isEmpty()) return RefreshResponseDto.failure(ErrorCode.TOKEN_INVALID);


            UserDetails userDetails = userRepository.findByEmail(userEmail)
                    .map(UserDetailsImpl::new)
                    .orElseThrow(() -> new UsernameNotFoundException(ErrorCode.INVALID_CREDENTIALS.getMessage()));

            String newAccessToken = jwtUtil.generateAccessToken(userDetails);
            String newRefreshToken = jwtUtil.generateRefreshToken(new HashMap<>(), userDetails);

            refreshTokenRepository.deleteByUser_Email(userEmail);

            RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
            refreshTokenEntity.setUser(userRepository.findByEmail(userEmail).get());
            refreshTokenEntity.setToken(newRefreshToken);
            refreshTokenEntity.setIp(request.getRemoteAddr());
            refreshTokenEntity.setDeviceInfo(request.getHeader("User-Agent"));
            refreshTokenEntity.setExpiryDate(LocalDateTime.now().plusDays(7));
            refreshTokenRepository.save(refreshTokenEntity);

            return RefreshResponseDto.success(newAccessToken, newRefreshToken);

        } catch (Exception e) {
            log.error("Error during refresh token: {}", e.getMessage());
            return RefreshResponseDto.failure(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

}

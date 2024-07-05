package com.mgnt.userservice.domain.service;

import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.exception.CustomException;
import com.mgnt.userservice.controller.dto.request.SignupRequestDto;
import com.mgnt.userservice.domain.entity.Users;
import com.mgnt.userservice.domain.repository.UserRepository;
import com.mgnt.userservice.event.AuthRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthenticationManager authenticationManager;
//    private final JwtUtil jwtUtil;

    @Transactional
    public void signup(SignupRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.EMAIL_DUPLICATED, null, Level.WARN);
        }

        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new CustomException(ErrorCode.PHONE_NUMBER_DUPLICATED, null, Level.WARN);
        }

        Users user = Users.builder()
                .email(request.email())
                .password(bCryptPasswordEncoder.encode(request.password()))
                .name(request.name())
                .role(request.role())
                .phoneNumber(request.phoneNumber())
                .address(request.address())
                .build();

        userRepository.save(user);
        try {
            eventPublisher.publishEvent(new AuthRegisteredEvent(user.getEmail(), user.getName()));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_ERROR, null, Level.ERROR);
        }
    }

//    @Override
//    public ResponseEntity<LoginResponseDto> login(LoginRequestDto dto, HttpServletRequest request) {
//        try {
//            // 사용자 정보 로드
//            UserDetails userDetails = userRepository.findByEmail(dto.getEmail())
//                    .map(UserDetailsImpl::new)
//                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + dto.getEmail()));
//
//            Users user = ((UserDetailsImpl) userDetails).getUser();
//            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword()))
//                return LoginResponseDto.failure(ErrorCode.LOGIN_FAILED);
//
//            // 이메일 인증 상태 확인
//            if (!user.getEmailVerified()) return LoginResponseDto.failure(ErrorCode.UNVERIFED_ACCOUNT);
//
//            // 이메일과 비밀번호를 사용하여 인증 시도
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
//            );
////            String accessToken = jwtUtil.generateAccessToken(userDetails);
//            String accessToken = jwtUtil.createAccessToken(dto.getEmail());
//            String refreshToken;
//
//            Optional<RefreshToken> existingTokenOpt = refreshTokenJpaRepository.findByUser_Email(dto.getEmail());
//            if (existingTokenOpt.isPresent()) {
//                RefreshToken existingToken = existingTokenOpt.get();
//                existingToken.setExpiryDate(LocalDateTime.now().plusDays(7));
//                refreshToken = existingToken.getToken();
//                refreshTokenJpaRepository.save(existingToken);
//            } else {
//                refreshToken = jwtUtil.createRefreshToken(dto.getEmail());
////                refreshToken = jwtUtil.generateRefreshToken(new HashMap<>(), userDetails);
//                RefreshToken refreshTokenEntity = RefreshToken.builder()
//                        .user(userJpaRepository.findByEmail(dto.getEmail()).get())
//                        .token(refreshToken)
//                        .ip(request.getRemoteAddr())
//                        .deviceInfo(request.getHeader("User-Agent"))
//                        .expiryDate(LocalDateTime.now().plusDays(7))
//                        .build();
//                refreshTokenJpaRepository.save(refreshTokenEntity);
//            }
//
//            return LoginResponseDto.success(accessToken, refreshToken);
//        } catch (Exception e) {
//            log.error("로그인 중 오류 발생: {}", e.getMessage());
//            return LoginResponseDto.failure(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
//        }
//    }
//
//    @Override
//    @Transactional
//    public ResponseEntity<LogoutResponseDto> logout(String accessToken) {
//        try {
//            if (accessToken == null || !accessToken.startsWith("Bearer ")) {
//                return LogoutResponseDto.failure(ErrorCode.INVALID_INPUT_VALUE.getCode(), ErrorCode.INVALID_INPUT_VALUE.getMessage());
//            }
//
//            String token = accessToken.substring(7);
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
//                return LogoutResponseDto.failure(ErrorCode.ACCESS_DENIED.getCode(), ErrorCode.ACCESS_DENIED.getMessage());
//            }
//
//            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//
//            try {
//                jwtUtil.getEmailFromToken(token); // 만료된 토큰일 경우 예외 발생
//                if (!jwtUtil.validateToken(token)) {
//                    return LogoutResponseDto.failure(ErrorCode.INVALID_TYPE_VALUE.getCode(), ErrorCode.INVALID_TYPE_VALUE.getMessage());
//                }
//            } catch (ExpiredJwtException e) {
//                log.warn("Expired token received during logout: {}", e.getMessage());
//                return LogoutResponseDto.failure(ErrorCode.ACCESS_DENIED.getCode(), ErrorCode.ACCESS_DENIED.getMessage());
//            }
//
//            SecurityContextHolder.clearContext();
//            refreshTokenJpaRepository.deleteByUser_Email(userDetails.getUsername());
//
//            return LogoutResponseDto.success();
//        } catch (Exception e) {
//            log.error("로그아웃 중 오류 발생: {}", e.getMessage());
//            return LogoutResponseDto.failure(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
//        }
//    }
//
//
//    @Override
//    @Transactional
//    public ResponseEntity<? super RefreshResponseDto> refresh(String rawToken, HttpServletRequest request) {
//        try {
//            if (rawToken == null || !rawToken.startsWith("Bearer "))
//                return RefreshResponseDto.failure(ErrorCode.BAD_REQUEST);
//
//
//            final String token = rawToken.substring(7);
//            String userEmail;
//
//            try {
//                userEmail = jwtUtil.getEmailFromToken(token);
//            } catch (ExpiredJwtException e) {
//                userEmail = e.getClaims().getSubject(); // 만료된 토큰에서 이메일을 추출
//            }
//
//            if (userEmail == null) return RefreshResponseDto.failure(ErrorCode.USER_UNAUTHORIZED);
//
//            Optional<RefreshToken> refreshTokenOpt = refreshTokenJpaRepository.findByUser_Email(userEmail);
//            if (refreshTokenOpt.isEmpty()) return RefreshResponseDto.failure(ErrorCode.INVALID_INPUT_VALUE);
//
//
//            UserDetails userDetails = userJpaRepository.findByEmail(userEmail)
//                    .map(UserDetailsImpl::new)
//                    .orElseThrow(() -> new UsernameNotFoundException(ErrorCode.INVALID_CREDENTIALS.getMessage()));
//
//            String newAccessToken = jwtUtil.createAccessToken(userDetails.getUsername());
//            String newRefreshToken = jwtUtil.createRefreshToken(userDetails.getUsername());
//
//            refreshTokenJpaRepository.deleteByUser_Email(userEmail);
//
//            RefreshToken refreshToken = RefreshToken.builder()
//                    .user(userJpaRepository.findByEmail(userEmail).get())
//                    .token(newRefreshToken)
//                    .ip(request.getRemoteAddr())
//                    .deviceInfo(request.getHeader("User-Agent"))
//                    .expiryDate(LocalDateTime.now().plusDays(7))
//                    .build();
//            refreshTokenJpaRepository.save(refreshToken);
//
//            return RefreshResponseDto.success(newAccessToken, newRefreshToken);
//
//        } catch (Exception e) {
//            log.error("Error during refresh token: {}", e.getMessage());
//            return RefreshResponseDto.failure(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
//        }
//    }

}

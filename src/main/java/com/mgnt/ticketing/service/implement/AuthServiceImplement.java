package com.mgnt.ticketing.service.implement;

import com.mgnt.ticketing.dto.ResponseDto;
import com.mgnt.ticketing.dto.request.auth.LoginRequestDto;
import com.mgnt.ticketing.dto.request.auth.SignUpRequestDto;
import com.mgnt.ticketing.dto.request.auth.UserRegisteredEvent;
import com.mgnt.ticketing.dto.response.ResponseCode;
import com.mgnt.ticketing.dto.response.ResponseMessage;
import com.mgnt.ticketing.dto.response.auth.LoginResponseDto;
import com.mgnt.ticketing.dto.response.auth.SignUpResponseDto;
import com.mgnt.ticketing.dto.response.auth.TokenReqRes;
import com.mgnt.ticketing.entity.RefreshTokenEntity;
import com.mgnt.ticketing.entity.UserEntity;
import com.mgnt.ticketing.entity.UserRoleEnum;
import com.mgnt.ticketing.repository.RefreshTokenRepository;
import com.mgnt.ticketing.repository.UserRepository;
import com.mgnt.ticketing.security.JwtUtils;
import com.mgnt.ticketing.security.UserDetailsImpl;
import com.mgnt.ticketing.service.AuthService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
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
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;


    @Override
    public ResponseEntity<? super SignUpResponseDto> signUp(SignUpRequestDto dto) {
        try {
            boolean hasEmail = userRepository.existsByEmail(dto.getEmail());
            if (hasEmail) {
                return SignUpResponseDto.duplicatedEmail();
            }

            UserEntity userEntity = UserEntity.from(dto, passwordEncoder.encode(dto.getPassword()));
            userRepository.save(userEntity);

            // 이벤트 발행
            eventPublisher.publishEvent(new UserRegisteredEvent(userEntity.getEmail(), userEntity.getName()));

            return SignUpResponseDto.success();
        } catch (Exception exception) {
            log.error("Database error: {}", exception.getMessage(), exception);
            return SignUpResponseDto.failure();
        }
    }

    @Override
    public ResponseEntity<? super LoginResponseDto> login(LoginRequestDto dto, HttpServletRequest request) {
        try {
            // 사용자 정보 로드
            UserDetails userDetails = userRepository.findByEmail(dto.getEmail())
                    .map(UserDetailsImpl::new)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + dto.getEmail()));

            // 이메일 인증 상태 확인
            UserEntity userEntity = ((UserDetailsImpl) userDetails).getUser();
            if (!userEntity.getEmailVerified()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        LoginResponseDto.failure(ResponseCode.UNVERIFIED_EMAIL, ResponseMessage.UNVERIFIED_EMAIL)
                );
            }

            // 이메일과 비밀번호를 사용하여 인증 시도
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );
            String accessToken = jwtUtils.generateAccessToken(userDetails);
            String refreshToken;

            Optional<RefreshTokenEntity> existingTokenOpt = refreshTokenRepository.findByUser_Email(dto.getEmail());
            if (existingTokenOpt.isPresent()) {
                RefreshTokenEntity existingToken = existingTokenOpt.get();
                existingToken.setExpiryDate(LocalDateTime.now().plusDays(7));
                refreshToken = existingToken.getToken();
                refreshTokenRepository.save(existingToken);
            } else {
                refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), userDetails);
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
            return LoginResponseDto.failure();
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseDto> logout(String accessToken) {
        try {
            if (accessToken == null || !accessToken.startsWith(JwtUtils.BEARER_PREFIX)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseDto(ResponseCode.VALIDATION_FAILED, ResponseMessage.VALIDATION_FAILED));
            }

            String token = accessToken.substring(JwtUtils.BEARER_PREFIX.length());

            // SecurityContextHolder에서 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseDto(ResponseCode.NO_PERMISSION, ResponseMessage.NO_PERMISSION));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            if (!jwtUtils.isTokenValid(token, userDetails)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseDto(ResponseCode.INVALID_REQUEST, ResponseMessage.INVALID_REQUEST));
            }

            // ContextHolder에서 사용자 정보 제거 및 refreshToken 삭제
            SecurityContextHolder.clearContext();
            refreshTokenRepository.deleteByUser_Email(userDetails.getUsername());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS));
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto(ResponseCode.INTERNAL_ERROR, ResponseMessage.INTERNAL_ERROR));
        }
    }

    @Transactional
    @Override
    public ResponseEntity<? super LoginResponseDto> refresh(String accessToken, HttpServletRequest request) {
        try {
            if (accessToken == null || !accessToken.startsWith(JwtUtils.BEARER_PREFIX)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDto(ResponseCode.VALIDATION_FAILED, ResponseMessage.VALIDATION_FAILED));
            }

            final String token = accessToken.substring(JwtUtils.BEARER_PREFIX.length());
            String userEmail = jwtUtils.extractUsername(token);

            if (userEmail == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto(ResponseCode.INVALID_REQUEST, ResponseMessage.INVALID_REQUEST));

            UserDetails userDetails = userRepository.findByEmail(userEmail)
                    .map(UserDetailsImpl::new)
                    .orElseThrow(() -> new UsernameNotFoundException(ResponseMessage.INVALID_CREDENTIALS));

            String newAccessToken = jwtUtils.generateAccessToken(userDetails);
            String newRefreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), userDetails);

            // Delete the old refresh token from the database
            refreshTokenRepository.deleteByUser_Email(userEmail);

            // Save the new refresh token in the database
            RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
            refreshTokenEntity.setUser(userRepository.findByEmail(userEmail).get());
            refreshTokenEntity.setToken(newRefreshToken);
            refreshTokenEntity.setIp(request.getRemoteAddr());
            refreshTokenEntity.setDeviceInfo(request.getHeader("User-Agent"));
            refreshTokenEntity.setExpiryDate(LocalDateTime.now().plusDays(7));
            refreshTokenRepository.save(refreshTokenEntity);

            return LoginResponseDto.success(newAccessToken, newRefreshToken);

        } catch (Exception e) {
            log.error("Error during refresh token: {}", e.getMessage());
            return LoginResponseDto.failure(ResponseCode.INTERNAL_ERROR, ResponseMessage.INTERNAL_ERROR);
        }
    }


}

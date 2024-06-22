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
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );

            UserDetails userDetails = userRepository.findByEmail(dto.getEmail())
                    .map(UserDetailsImpl::new)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + dto.getEmail()));

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

    public TokenReqRes refreshToken(TokenReqRes request) {
        TokenReqRes response = new TokenReqRes();

        try {
            UserDetails userDetails = userRepository.findByEmail(request.getEmail())
                    .map(UserDetailsImpl::new)
                    .orElseThrow(() -> new UsernameNotFoundException(ResponseMessage.INVALID_CREDENTIALS));

            String accessToken = jwtUtils.generateAccessToken(userDetails);
            String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), userDetails);

            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setStatusCode(200);
            response.setMessage("RefreshToken이 성공적으로 발급 되었습니다");

        } catch (Exception e) {
            log.error("refresh token 발급 중 오류 발생: {}", e.getMessage());
            response.setStatusCode(500);
            response.setMessage("refresh token 발급 중 오류가 발생했습니다.");
        }
        return response;
    }

}

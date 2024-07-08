package com.mgnt.userservice.domain.service;

import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.exception.CustomException;
import com.mgnt.userservice.controller.dto.request.LoginRequestDto;
import com.mgnt.userservice.controller.dto.request.SignupRequestDto;
import com.mgnt.userservice.controller.dto.response.LoginResponseDto;
import com.mgnt.userservice.controller.dto.response.RefreshTokenResponseDto;
import com.mgnt.userservice.domain.entity.Users;
import com.mgnt.userservice.domain.repository.UserRepository;
import com.mgnt.userservice.utils.JwtUtil;
import com.mgnt.userservice.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RedisUtils redisUtils;

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

        try {
            emailService.sendVerificationEmail(user.getEmail(), user.getName());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_ERROR, null, Level.ERROR);
        }

        userRepository.save(user);

    }

    public LoginResponseDto login(LoginRequestDto dto) {
        try {
            Users user = userRepository.findUserByEmail(dto.email())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + dto.email()));

            if (!bCryptPasswordEncoder.matches(dto.password(), user.getPassword())) {
                throw new CustomException(ErrorCode.LOGIN_FAILED, null, Level.INFO);
            }

            if (!user.getEmailVerified()) {
                throw new CustomException(ErrorCode.UNVERIFED_ACCOUNT, null, Level.INFO);
            }

            String accessToken = jwtUtil.createAccessToken(user.getEmail(), user.getUserId(), user.getRole().name());
            String refreshToken = jwtUtil.createRefreshToken(user.getEmail(), user.getUserId(), user.getRole().name());


            // Redis에 리프레시 토큰 저장 (7일 동안)
            String redisKey = "RT:" + user.getEmail();
            redisUtils.setData(
                    redisKey,
                    refreshToken,
                    86400000 * 7 //7일( 1일을 초로 나타내고 그것을 7로 곱함)
            );
            if (!redisUtils.getCode(redisKey).equals(refreshToken))
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, null, Level.ERROR);

            return new LoginResponseDto(
                    user.getUserId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole().name(),
                    accessToken,
                    refreshToken
            );
        } catch (Exception e) {
            log.error("Login error: ", e);
            throw new CustomException(ErrorCode.LOGIN_FAILED, null, Level.INFO);
        }
    }


    //    // mysql database에 등록된 PK가 userId, role이 userRole에 해당한다.
//    // 현재 logout 코드는 대대적인 수정이 필요하다. 회원가입과 로그인 코드 처럼 일관성있는 예외처리와 반환값 처리 redis를 이용한 데이터 삭제도 필요하다.
    @Transactional
    public void logout(String userId, String accessToken) {
        Users user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, null, Level.INFO));

        redisUtils.deleteKey("RT:" + user.getEmail());
        Long remainingTimeInMillis = jwtUtil.getRemainingTime(accessToken);
        redisUtils.addToBlacklist(accessToken, remainingTimeInMillis);
    }

    //    // 아래 코드는 수정이 필요하다.
//    // 메서드의 시그니처만 유효하며 scope영역은 대부분 수정해야 한다.
//    // accessToken을 새로 발행하고, 기존 redis에 저장된 refreshToken을 삭제하거나 정보를 업데이트 해준다.
    @Transactional
    public RefreshTokenResponseDto refresh(String userId, String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN, null, Level.INFO);
        }
        Users user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, null, Level.INFO));

        String newAccessToken = jwtUtil.createAccessToken(user.getEmail(), user.getUserId(), user.getRole().name());
        String newRefreshToken = jwtUtil.createRefreshToken(user.getEmail(), user.getUserId(), user.getRole().name());

        // Redis에 리프레시 토큰 업데이트
        String redisKey = "RT:" + user.getEmail();
        redisUtils.setData(redisKey, newRefreshToken, 7 * 24 * 60 * 60);

        return new RefreshTokenResponseDto(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                newAccessToken,
                newRefreshToken);
    }

}

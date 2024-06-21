package com.mgnt.ticketing.service.implement;

import com.mgnt.ticketing.dto.ResponseDto;
import com.mgnt.ticketing.dto.request.auth.SignUpRequestDto;
import com.mgnt.ticketing.dto.request.auth.UserRegisteredEvent;
import com.mgnt.ticketing.dto.response.ResponseCode;
import com.mgnt.ticketing.dto.response.ResponseMessage;
import com.mgnt.ticketing.dto.response.auth.SignUpResponseDto;
import com.mgnt.ticketing.dto.response.auth.TokenReqRes;
import com.mgnt.ticketing.entity.UserEntity;
import com.mgnt.ticketing.entity.UserRoleEnum;
import com.mgnt.ticketing.repository.UserRepository;
import com.mgnt.ticketing.security.JwtUtils;
import com.mgnt.ticketing.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImplement implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;


    @Override
    public ResponseEntity<? super SignUpResponseDto> signUp(SignUpRequestDto dto) {
        try {
            boolean hasEmail = userRepository.existsByEmail(dto.getEmail());
            if (hasEmail) {
                return ResponseEntity
                        .status(409)
                        .body(SignUpResponseDto.builder()
                                .code(ResponseCode.DUPLICATED_EMAIL)
                                .message(ResponseMessage.DUPLICATED_EMAIL)
                                .build());
            }

            UserEntity userEntity = UserEntity.from(dto, passwordEncoder.encode(dto.getPassword()));
            userRepository.save(userEntity);

            // 이벤트 발행
            eventPublisher.publishEvent(new UserRegisteredEvent(userEntity.getEmail(), userEntity.getName()));

            return ResponseEntity.ok(SignUpResponseDto.builder()
                    .code(ResponseCode.SUCCESS)
                    .message(ResponseMessage.SIGN_UP_SUCCESS)
                    .build());
        } catch (Exception exception) {
            log.error("Database error: {}", exception.getMessage(), exception);
            return ResponseEntity
                    .status(500)
                    .body(SignUpResponseDto.builder()
                            .code(ResponseCode.DATABASE_ERROR)
                            .message(ResponseMessage.DATABASE_ERROR)
                            .build());
        }
    }

//    public TokenReqRes login(TokenReqRes request) {
//        TokenReqRes response = new TokenReqRes();
//
//        try {
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
//            );
//            UserDetails userDetails = userRepository.findByEmail(request.getEmail()).orElseThrow();
//            String accessToken = jwtUtils.generateAccessToken(userDetails);
//            String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), userDetails);
//
//            response.setAccessToken(accessToken);
//            response.setRefreshToken(refreshToken);
//            response.setStatusCode(200);
//            response.setExpirationTime("24Hr");
//            response.setMessage("로그인 성공적하였습니다.");
//
//        } catch (Exception e) {
//            log.error("로그인 중 오류 발생: {}", e.getMessage());
//            response.setStatusCode(500);
//            response.setMessage("로그인 중 오류가 발생했습니다.");
//        }
//        return response;
//    }
//
//    public TokenReqRes refreshToken(TokenReqRes request) {
//        TokenReqRes response = new TokenReqRes();
//
//        try {
//            UserDetails userDetails = userRepository.findByEmail(request.getEmail()).orElseThrow();
//            String accessToken = jwtUtils.generateAccessToken(userDetails);
//            String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), userDetails);
//
//            response.setAccessToken(accessToken);
//            response.setRefreshToken(refreshToken);
//            response.setStatusCode(200);
//            response.setExpirationTime("7Days");
//            response.setMessage("RefreshToken이 성공적으로 발급 되었습니다");
//
//        } catch (Exception e) {
//            log.error("refresh token 발급 중 오류 발생: {}", e.getMessage());
//            response.setStatusCode(500);
//            response.setMessage("refresh token 발급 중 오류가 발생했습니다.");
//        }
//        return response;
//    }

}

package com.mgnt.core.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
//import com.mgnt.userservice.domain.service.UserReader;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService implements JwtInterface {

//    private final UserReader userReader;

    @Override
    public String createToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JwtConstants.EXP_TIME)) // 만료 시간 설정
                .signWith(this.getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(JwtConstants.SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String getToken() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader("Authorization");
    }

    @Override
    public void sendToken(HttpServletResponse response, String token) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Authorization", "Bearer " + token); // Bearer 토큰 형식 명시
        log.info("Authorization 헤더 설정 완료: Bearer {}", token);
    }

    @Override
    public Long getUserId() throws Exception {
        // 토큰 추출
        String accessToken = getToken();
        if (!StringUtils.hasText(accessToken)) {
            throw new Exception("토큰이 비어있습니다.");
        }

        // 토큰 파싱
        Jws<Claims> claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(JwtConstants.SECRET_KEY.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(accessToken);
        } catch (Exception e) {
            log.error("토큰 파싱 중 오류 발생", e);
            throw new Exception("토큰이 유효하지 않습니다.");
        }

        log.info("claims.getBody : {}", claims.getBody());
        log.info("claims.getHeader : {}", claims.getHeader());

        // userId 추출
        String subject = claims.getBody().getSubject();
        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException e) {
            log.error("userId 변환 중 오류 발생", e);
            throw new Exception("userId 형식이 올바르지 않습니다.");
        }
    }

    @Override
    public boolean validToken(Long userId) {
        // TODO : 서버 기동을 위해 임시로 주석처리
        // 사용자 존재 여부 확인
//        return userReader.findUser(userId) != null;
        return true;
    }
}

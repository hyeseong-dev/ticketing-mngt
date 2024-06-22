package com.mgnt.ticketing.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

/**
 * JWT 토큰을 생성하고 검증하는 클래스입니다.
 */
@Slf4j
@Component
public class JwtUtils {

    /**
     * Authorization 헤더의 키값
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * 사용자 권한 값을 나타내는 키
     */
    public static final String AUTHORIZATION_KEY = "auth";

    /**
     * 토큰의 식별자 (Bearer)
     */
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * 액세스 토큰의 유효기간 (1일: 86400000ms)
     */
    public final long ACCESS_TOKEN_EXPIRATION_TIME = 24 * 60 * 60 * 1000L; // 1일

    /**
     * 리프레시 토큰의 유효기간 (7일: 604800000ms)
     */
    public final long REFRESH_TOKEN_EXPIRATINO_TIME = 7 * 24 * 60 * 60 * 1000L; // 7일

    /**
     * 에러를 처리하는 뷰
     */
    public final View error;

    /**
     * BASE64로 인코딩된 SecretKey 값
     */
    @Value("${jwt.secret.key}")
    public String secretKey;
    public SecretKey key;
    public final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    /**
     * JwtTokenProvider 생성자
     *
     * @param error 에러를 처리하는 뷰 객체
     */
    public JwtUtils(View error) {
        this.error = error;
    }

    /**
     * 초기화 메서드로, SecretKey를 BASE64 디코딩하여 초기화합니다.
     */
    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    /**
     * 액세스 토큰을 생성합니다.
     *
     * @param userDetails 사용자 정보를 담은 UserDetails 객체
     * @return 생성된 액세스 토큰 문자열
     */
    public String generateAccessToken(UserDetails userDetails) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }


    /**
     * 리프레시 토큰을 생성합니다.
     *
     * @param claims       사용자 정보를 담은 클레임
     * @param userDetails 사용자 정보를 담은 UserDetails 객체
     * @return 생성된 리프레시 토큰 문자열
     */
    public String generateRefreshToken(HashMap<String, Object> claims, UserDetails userDetails) {
        Date date = new Date();
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(date.getTime() + REFRESH_TOKEN_EXPIRATINO_TIME))
                .signWith(key)
                .compact();
    }

    /**
     * 토큰에서 사용자 이름을 추출합니다.
     *
     * @param token JWT 토큰 문자열
     * @return 추출된 사용자 이름
     */
    public String extractUsername(String token){
        return extractClaims(token, Claims::getSubject);
    }

    /**
     * 토큰에서 클레임을 추출합니다.
     *
     * @param token 토큰 문자열
     * @param claimsTFunction 클레임을 처리하는 함수
     * @param <T> 클레임의 타입
     * @return 클레임의 값
     */
    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction){
        return claimsTFunction.apply(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload());
    }

    /**
     * 토큰의 유효성을 검증합니다.
     *
     * @param token JWT 토큰 문자열
     * @param userDetails 사용자 정보를 담은 UserDetails 객체
     * @return 토큰이 유효한지 여부
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) &&! isTokenExpired(token));
    }

    /**
     * 토큰의 만료 여부를 검증합니다.
     *
     * @param token JWT 토큰 문자열
     * @return 토큰이 만료되었는지 여부
     */
    private boolean isTokenExpired(String token) {
        return extractClaims(token, Claims::getExpiration).before(new Date());
    }
}

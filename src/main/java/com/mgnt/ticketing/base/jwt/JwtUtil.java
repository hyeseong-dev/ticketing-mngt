package com.mgnt.ticketing.base.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT 토큰을 생성하고 검증하는 클래스입니다.
 */
@Slf4j
@Component
public class JwtUtil {

    // Constants and configuration values
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_KEY = "email";
    public static final String BEARER_PREFIX = "Bearer ";
    public final long ACCESS_TOKEN_EXPIRATION_TIME = 24 * 60 * 60 * 1000L; // 1일
    public final long REFRESH_TOKEN_EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000L; // 7일

    @Value("${jwt.secret.key}")
    public String secretKey;
    public SecretKey key;
    public final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    // Generate access token with claims
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(AUTHORIZATION_KEY, userDetails.getUsername());
        return createToken(claims, userDetails.getUsername(), ACCESS_TOKEN_EXPIRATION_TIME);
    }

    // Generate refresh token
    public String generateRefreshToken(Map<String, Object> claims, UserDetails userDetails) {
        return createToken(claims, userDetails.getUsername(), REFRESH_TOKEN_EXPIRATION_TIME);
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationTime) {
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationTime))
                .signWith(key, signatureAlgorithm)
                .compact();
    }

    // Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract claims from token
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    // Validate token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Check if token is expired
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // Get token from request
    public String getTokenFromRequest(HttpServletRequest request) {
        return request.getHeader(AUTHORIZATION_HEADER);
    }

    // Remove Bearer prefix from token
    public String substringToken(String token) {
        if (token.startsWith(BEARER_PREFIX)) {
            return token.substring(BEARER_PREFIX.length());
        }
        return token;
    }

    // Extract user info from token
    public Claims getUserInfoFromToken(String token) {
        return extractAllClaims(token);
    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }
}

package com.mgnt.userservice.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.app.jwtSecretKey}")
    private String SECRET_KEY;

    private SignatureAlgorithm SIGNATURE_ALGORITHMS = SignatureAlgorithm.HS256;

    @Value("${jwt.access-token-validity}")
    private long ACCES_TOKEN_VALIDITY_IN_MILLISECONDS;

    @Value("${jwt.refresh-token-validity}")
    private long REFRESH_TOKEN_VALIDITY_IN_MILLISECONDS;

    private SecretKey key;

    public JwtUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        this.key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), SIGNATURE_ALGORITHMS.getJcaName());
    }

    public String createAccessToken(String email, Long userId, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + ACCES_TOKEN_VALIDITY_IN_MILLISECONDS);
        System.out.println("Access Token 발급 시점 (now): " + now);
        System.out.println("Access Token 만료 시점 (validity): " + validity);
        
        return Jwts.builder()
                .setSubject(email)
                .claim("id", userId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SIGNATURE_ALGORITHMS)
                .compact();
    }

    public String createRefreshToken(String email, Long userId, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + REFRESH_TOKEN_VALIDITY_IN_MILLISECONDS);

        return Jwts.builder()
                .setSubject(email)
                .claim("id", userId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SIGNATURE_ALGORITHMS)
                .compact();
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T getClaimFromToken(String token, String claimName, Class<T> requiredType) {
        return getAllClaimsFromToken(token).get(claimName, requiredType);
    }

    public boolean isTokenExpired(String token) {
        return getAllClaimsFromToken(token).getExpiration().before(new Date());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public SecretKey getKey() {
        return this.key;
    }

    public void addToBlacklist(String token, long expirationTime) {
        redisTemplate.opsForValue().set("BL_" + token, "blacklisted", expirationTime, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("BL_" + token));
    }

    public long getRemainingTime(String token) {
        Date expiration = getAllClaimsFromToken(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }
}

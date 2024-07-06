package com.mgnt.gatewayservice.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final RedisTemplate<String, String> redisTemplate;

    public JwtUtil(@Value("${jwt.app.jwtSecretKey}") String secretKey, RedisTemplate<String, String> redisTemplate) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.redisTemplate = redisTemplate;
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

    public String getSubjectFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
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

    public Long getRemainingTime(String token) {
        Date expiration = getAllClaimsFromToken(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }
}
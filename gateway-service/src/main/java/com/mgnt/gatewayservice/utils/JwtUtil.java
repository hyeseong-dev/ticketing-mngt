package com.mgnt.gatewayservice.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey key;
    private final RedisTemplate<String, String> redisTemplate;
    private SignatureAlgorithm SIGNATURE_ALGORITHMS = SignatureAlgorithm.HS256;

    public JwtUtil(@Value("${jwt.app.jwtSecretKey}") String secretKey, RedisTemplate<String, String> redisTemplate) {
        this.key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), SIGNATURE_ALGORITHMS.getJcaName());
        this.redisTemplate = redisTemplate;
    }

    public Claims getAllClaimsFromToken(String token) {
        Claims claims = Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims;
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
        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Invalidate Token: {}", e.getMessage());
        }
        return false;
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

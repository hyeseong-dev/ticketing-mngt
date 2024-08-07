package com.mgnt.userservice.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisUtils {
    private final RedisTemplate<String, String> redisTemplate;

    public void setData(String key, String value, long duration) {
        Duration expireDuration = Duration.ofSeconds(duration);
        redisTemplate.opsForValue().set(key, value, expireDuration);
    }

    public String getCode(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public boolean existKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    public void addToBlacklist(String token, long durationInMillis) {
        redisTemplate.opsForValue().set(
                "BL_" + token,
                "blacklisted",
                durationInMillis,
                TimeUnit.MILLISECONDS
        );
    }
}
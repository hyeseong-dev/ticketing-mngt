package com.mgnt.reservationservice.domain.repository.impl;

import com.mgnt.reservationservice.domain.repository.QueueRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
@RequiredArgsConstructor
public class QueueRedisRepositoryImpl implements QueueRedisRepository {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Long addToQueue(String queueKey, String userId) {
        Double score = (double) System.currentTimeMillis();
        Boolean added = redisTemplate.opsForZSet().add(queueKey, userId, score);
        if (Boolean.TRUE.equals(added)) {
            return redisTemplate.opsForZSet().rank(queueKey, userId);
        }
        return null;
    }

    @Override
    public Long getQueuePosition(String queueKey, String userId) {
        Long rank = redisTemplate.opsForZSet().rank(queueKey, userId);
        return (rank != null) ? rank + 1 : null;
    }

    @Override
    public void removeFromQueue(String queueKey, String userId) {
        redisTemplate.opsForZSet().remove(queueKey, userId);
    }

    @Override
    public Long popUserFromQueue(String queueKey) {
        Set<String> userIds = redisTemplate.opsForZSet().range(queueKey, 0, 0);
        if (userIds == null || userIds.isEmpty()) {
            return null;
        }
        String userId = userIds.iterator().next();
        redisTemplate.opsForZSet().remove(queueKey, userId);
        return Long.valueOf(userId);
    }

    @Override
    public boolean setAccessToken(String tokenKey, String accessToken, int expirationMinutes) {
        log.debug("Attempting to set access token. Key: {}, Token: {}, Expiration: {} minutes", tokenKey, accessToken, expirationMinutes);
        boolean result = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(tokenKey, accessToken, expirationMinutes, TimeUnit.MINUTES));
        log.debug("Result of setting access token. Key: {}, Result: {}", tokenKey, result);
        return result;
    }

    @Override
    public boolean setAttemptCount(String countKey, int initialCount, int expirationHours) {
        log.debug("Attempting to set attempt count. Key: {}, Initial Count: {}, Expiration: {} hours", countKey, initialCount, expirationHours);
        boolean result = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(countKey, String.valueOf(initialCount), expirationHours, TimeUnit.HOURS));
        log.debug("Result of setting attempt count. Key: {}, Result: {}", countKey, result);
        return result;
    }

    @Override
    public String getAccessToken(String tokenKey) {
        return redisTemplate.opsForValue().get(tokenKey);
    }

    @Override
    public Set<String> getTopUsers(String queueKey, int count) {
        return redisTemplate.opsForZSet().range(queueKey, 0, count - 1);
    }

    @Override
    public Long getAccessTokenTTL(String tokenKey) {
        return redisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);
    }
}

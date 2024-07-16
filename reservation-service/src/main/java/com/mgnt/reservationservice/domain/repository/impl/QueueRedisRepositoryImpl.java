package com.mgnt.reservationservice.domain.repository.impl;

import com.mgnt.reservationservice.domain.repository.QueueRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

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
}

package com.mgnt.reservationservice.domain.repository;

import java.util.Set;

public interface QueueRedisRepository {

    Long addToQueue(String queueKey, String userId);

    Long getQueuePosition(String queueKey, String userId);

    void removeFromQueue(String queueKey, String userId);

    Long popUserFromQueue(String queueKey);

    boolean setAccessToken(String userId, String accessToken, int expirationMinutes);

    boolean setAttemptCount(String userId, int initialCount, int expirationHours);

    String getAccessToken(String tokenKey);

    Set<String> getTopUsers(String queueKey, int count);

    Long getAccessTokenTTL(String tokenKey);
}

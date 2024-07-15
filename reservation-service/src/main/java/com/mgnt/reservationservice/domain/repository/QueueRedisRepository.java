package com.mgnt.reservationservice.domain.repository;

public interface QueueRedisRepository {
    
    Long addToQueue(String queueKey, String userId);

    Long getQueuePosition(String queueKey, String userId);
}

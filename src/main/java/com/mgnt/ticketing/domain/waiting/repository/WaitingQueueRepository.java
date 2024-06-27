package com.mgnt.ticketing.domain.waiting.repository;

import com.mgnt.ticketing.domain.waiting.entity.WaitingQueue;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface WaitingQueueRepository {

    WaitingQueue findByUserId(Long userId);

    long countByStatusIs(WaitingQueue.Status status);

    void save(WaitingQueue waitingQueue);

    WaitingQueue findByUserIdAndToken(Long userId, String token);

    List<WaitingQueue> findAllByRequestTimeBeforeAndStatusIs(Timestamp expireBefore, WaitingQueue.Status status);

    List<WaitingQueue> findByStatusIsOrderByRequestTimeAsc(WaitingQueue.Status status, PageRequest pageRequest);
}

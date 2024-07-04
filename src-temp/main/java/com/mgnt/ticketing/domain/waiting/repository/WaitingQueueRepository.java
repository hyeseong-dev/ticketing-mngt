package com.mgnt.ticketing.domain.waiting.repository;

import com.mgnt.ticketing.domain.waiting.entity.WaitingQueue;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/**
 * 대기열 리포지토리 인터페이스
 *
 * 대기열 관련 데이터베이스 작업을 처리하는 메서드를 정의합니다.
 */
@Repository
public interface WaitingQueueRepository {

    WaitingQueue findByUserId(Long userId);

    long countByStatusIs(WaitingQueue.Status status);

    long countByRequestTimeBeforeAndStatusIs(WaitingQueue.Status status, Timestamp requestTime);

    void save(WaitingQueue waitingQueue);

    WaitingQueue findByUserIdAndToken(Long userId, String token);

    List<WaitingQueue> findAllByRequestTimeBeforeAndStatusIs(Timestamp expireBefore, WaitingQueue.Status status);

    List<WaitingQueue> findByStatusIsOrderByRequestTimeAsc(WaitingQueue.Status status, PageRequest pageRequest);
}

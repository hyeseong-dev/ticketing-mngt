package com.mgnt.ticketing.domain.waiting.repository;

import com.mgnt.ticketing.domain.waiting.entity.WaitingQueue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;

/**
 * 대기열 JPA 리포지토리 인터페이스
 *
 * JpaRepository를 상속받아 대기열 엔티티의 기본 CRUD 작업을 처리합니다.
 */
public interface WaitingQueueJpaRepository extends JpaRepository<WaitingQueue, Long> {

    WaitingQueue findByUserId(Long userId);

    long countByStatusIs(WaitingQueue.Status status);

    WaitingQueue findByUserIdAndToken(Long userId, String token);

    List<WaitingQueue> findAllByRequestTimeBeforeAndStatusIs(Timestamp expireBefore, WaitingQueue.Status status);

    List<WaitingQueue> findByStatusIsOrderByRequestTimeAsc(WaitingQueue.Status status, Pageable pageable);

    long countByRequestTimeBeforeAndStatusIs(Timestamp requestTime, WaitingQueue.Status status);
}

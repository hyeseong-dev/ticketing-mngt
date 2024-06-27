package com.mgnt.ticketing.infra_structure;

import com.mgnt.ticketing.domain.waiting.entity.WaitingQueue;
import com.mgnt.ticketing.domain.waiting.repository.WaitingQueueJpaRepository;
import com.mgnt.ticketing.domain.waiting.repository.WaitingQueueRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/**
 * 대기열 리포지토리 구현 클래스
 *
 * 이 클래스는 WaitingQueueRepository 인터페이스를 구현하며, 대기열 관련 데이터베이스 작업을 처리합니다.
 */
@Repository
public class WaitingQueueRepositoryImpl implements WaitingQueueRepository {

    private final WaitingQueueJpaRepository waitingQueueJpaRepository;

    public WaitingQueueRepositoryImpl(WaitingQueueJpaRepository waitingQueueJpaRepository) {
        this.waitingQueueJpaRepository = waitingQueueJpaRepository;
    }

    /**
     * 사용자 ID로 대기열 항목 조회
     *
     * @param userId 사용자 ID
     * @return 대기열 항목
     */
    @Override
    public WaitingQueue findByUserId(Long userId) {
        return waitingQueueJpaRepository.findByUserId(userId);
    }

    /**
     * 특정 상태의 대기열 항목 수 조회
     *
     * @param status 대기열 상태
     * @return 대기열 항목 수
     */
    @Override
    public long countByStatusIs(WaitingQueue.Status status) {
        return waitingQueueJpaRepository.countByStatusIs(status);
    }

    /**
     * 대기열 항목 저장
     *
     * @param waitingQueue 대기열 항목
     */
    @Override
    public void save(WaitingQueue waitingQueue) {
        waitingQueueJpaRepository.save(waitingQueue);
    }

    /**
     * 사용자 ID와 토큰으로 대기열 항목 조회
     *
     * @param userId 사용자 ID
     * @param token  토큰
     * @return 대기열 항목
     */
    @Override
    public WaitingQueue findByUserIdAndToken(Long userId, String token) {
        return waitingQueueJpaRepository.findByUserIdAndToken(userId, token);
    }

    /**
     * 특정 시간 이전에 요청된 특정 상태의 대기열 항목 조회
     *
     * @param expireBefore 만료 이전 시간
     * @param status       대기열 상태
     * @return 대기열 항목 목록
     */
    @Override
    public List<WaitingQueue> findAllByRequestTimeBeforeAndStatusIs(Timestamp expireBefore, WaitingQueue.Status status) {
        return waitingQueueJpaRepository.findAllByRequestTimeBeforeAndStatusIs(expireBefore, status);
    }

    /**
     * 특정 상태의 대기열 항목을 요청 시간 순으로 페이지네이션하여 조회
     *
     * @param status      대기열 상태
     * @param pageRequest 페이지 요청 정보
     * @return 대기열 항목 목록
     */
    @Override
    public List<WaitingQueue> findByStatusIsOrderByRequestTimeAsc(WaitingQueue.Status status, PageRequest pageRequest) {
        return waitingQueueJpaRepository.findByStatusIsOrderByRequestTimeAsc(status, pageRequest);
    }
}

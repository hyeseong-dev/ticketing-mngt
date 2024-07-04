package com.mgnt.ticketing.domain.waiting.service;

import com.mgnt.ticketing.domain.waiting.WaitingConstants;
import com.mgnt.ticketing.domain.waiting.entity.WaitingQueue;
import com.mgnt.ticketing.domain.waiting.repository.WaitingQueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

/**
 * 대기열 처리 스케줄러
 *
 * 이 클래스는 대기열 항목을 주기적으로 확인하고 만료된 항목을 처리하며,
 * 다음 순번의 대기열 항목을 활성화하는 작업을 수행합니다.
 */
@Component
@RequiredArgsConstructor
public class WaitingScheduler {

    private final WaitingQueueRepository waitingQueueRepository;

    /**
     * 대기열 만료 처리 메서드
     *
     * 이 메서드는 매 2초마다 실행되어 현재 시간 기준으로 자동 만료 시간이 지난 대기열 항목을 만료 처리합니다.
     */
    @Scheduled(fixedRate = 2000) // 매 2초마다 실행
    public void expireWaitingQueue() {
        // 현재 시간 기준으로 자동 만료 시간(5분) 전 시간 계산
        Timestamp expireBefore = new Timestamp(System.currentTimeMillis() - WaitingConstants.AUTO_EXPIRED_MILLIS);

        // 만료되어야 하는 대기열 항목 조회
        List<WaitingQueue> expiredTargets = waitingQueueRepository.findAllByRequestTimeBeforeAndStatusIs(expireBefore, WaitingQueue.Status.ACTIVE);

        // 만료 처리
        for (WaitingQueue entry : expiredTargets) {
            entry.expiredToken();
            waitingQueueRepository.save(entry);
        }

        // 다음 n개 순번 사용자 활성화 로직 호출
        activateNextUser(expiredTargets.size());
    }

    /**
     * 다음 순번의 대기열 항목을 활성화하는 메서드
     *
     * @param cnt 활성화할 사용자 수
     */
    public void activateNextUser(int cnt) {
        // 대기 상태의 사용자 중 요청 시간이 빠른 순서대로 cnt만큼 조회
        List<WaitingQueue> waitingUsers = waitingQueueRepository.findByStatusIsOrderByRequestTimeAsc(WaitingQueue.Status.WAITING, PageRequest.of(0, cnt));

        if (!waitingUsers.isEmpty()) {
            // 다음 순번 유저를 활성화
            waitingUsers.forEach(WaitingQueue::toActive);
        }
    }

}
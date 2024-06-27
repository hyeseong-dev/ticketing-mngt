package com.mgnt.ticketing.domain.waiting.service;

import com.mgnt.ticketing.base.jwt.JwtService;
import com.mgnt.ticketing.controller.waiting.response.CheckActiveResponse;
import com.mgnt.ticketing.controller.waiting.response.IssueTokenResponse;
import com.mgnt.ticketing.domain.waiting.WaitingConstants;
import com.mgnt.ticketing.domain.waiting.entity.WaitingQueue;
import com.mgnt.ticketing.domain.waiting.repository.WaitingQueueRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 대기열 서비스 클래스
 *
 * 이 클래스는 대기열과 관련된 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
public class WaitingService implements WaitingInterface {

    private final WaitingQueueRepository waitingQueueRepository;
    private final JwtService jwtService;

    /**
     * 토큰 발급 메서드
     *
     * @param userId 사용자 ID
     * @return 토큰 발급 응답
     */
    @Override
    public IssueTokenResponse issueToken(Long userId) {
        return new IssueTokenResponse(jwtService.createToken(userId));
    }

    /**
     * 대기열 추가 메서드
     *
     * 첫 진입 시 또는 새로고침 시 호출
     *
     * @param userId 사용자 ID
     * @param token  토큰
     * @return 활성 상태 확인 응답
     */
    @Override
    public CheckActiveResponse addWaitingQueue(Long userId, String token) {
        Long waitingNum = null;
        Long expectedWaitTimeInSeconds = null;

        // 기존 토큰 있으면 만료시킴
        expiredIfExist(userId);

        // 대기열 활성 유저 수 확인
        long activeSize = waitingQueueRepository.countByStatusIs(WaitingQueue.Status.ACTIVE);
        boolean isActive = activeSize < WaitingConstants.ACTIVE_USER_CNT;
        if (isActive) {
            // 유저 진입 활성화
            waitingQueueRepository.save(WaitingQueue.toActiveEntity(userId, token));
        } else {
            // 유저 비활성, 대기열 정보 생성
            waitingNum = activeSize - WaitingConstants.ACTIVE_USER_CNT;
            expectedWaitTimeInSeconds = Duration.ofMinutes(waitingNum).toSeconds();
            waitingQueueRepository.save(WaitingQueue.toWaitingEntity(userId, token));
        }

        return new CheckActiveResponse(
                isActive,
                new CheckActiveResponse.WaitingTicketInfo(waitingNum, expectedWaitTimeInSeconds)
        );
    }

    /**
     * 기존 토큰 만료 메서드
     *
     * @param userId 사용자 ID
     */
    public void expiredIfExist(Long userId) {
        WaitingQueue existingQueue = waitingQueueRepository.findByUserId(userId);
        if (existingQueue != null) {
            existingQueue.expiredToken();
        }
    }

    /**
     * 대기열 확인 메서드
     *
     * @param userId 사용자 ID
     * @param token  토큰
     * @return 활성 상태 확인 응답
     */
    @Override
    public CheckActiveResponse checkActive(Long userId, String token) {
        Long waitingNum = null;
        Long expectedWaitTimeInSeconds = null;

        // 내 대기 상태 확인
        WaitingQueue waitingQueue = waitingQueueRepository.findByUserIdAndToken(userId, token);
        if (waitingQueue == null || waitingQueue.getStatus().equals(WaitingQueue.Status.EXPIRED)) {
            throw new EntityNotFoundException("새로고침하여 다시 진입하세요.");
        }

        // 활성 여부, 대기열 정보 반환
        boolean isActive = waitingQueue.getStatus().equals(WaitingQueue.Status.ACTIVE);
        if (!isActive) {
            // 대기열 정보 생성
            long activeSize = waitingQueueRepository.countByStatusIs(WaitingQueue.Status.ACTIVE);
            waitingNum = activeSize - WaitingConstants.ACTIVE_USER_CNT;
            expectedWaitTimeInSeconds = Duration.ofMinutes(waitingNum).toSeconds();
        }

        return new CheckActiveResponse(
                isActive,
                new CheckActiveResponse.WaitingTicketInfo(waitingNum, expectedWaitTimeInSeconds)
        );
    }
}

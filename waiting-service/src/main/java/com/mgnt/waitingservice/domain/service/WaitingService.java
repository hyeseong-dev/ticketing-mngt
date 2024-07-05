//package com.mgnt.ticketing.domain.waiting.service;
//
//import com.mgnt.ticketing.base.jwt.JwtService;
//import com.mgnt.ticketing.controller.waiting.response.CheckActiveResponse;
//import com.mgnt.ticketing.controller.waiting.response.IssueTokenResponse;
//import com.mgnt.ticketing.domain.waiting.WaitingConstants;
//import com.mgnt.ticketing.domain.waiting.entity.WaitingQueue;
//import com.mgnt.ticketing.domain.waiting.repository.WaitingQueueRepository;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Duration;
//
///**
// * 대기열 서비스 클래스
// *
// * 이 클래스는 대기열과 관련된 비즈니스 로직을 처리합니다.
// */
//@Service
//@RequiredArgsConstructor
//public class WaitingService implements WaitingInterface {
//
//    // 대기열 레포지토리
//    private final WaitingQueueRepository waitingQueueRepository;
//    // JWT 서비스
//    private final JwtService jwtService;
//
//    /**
//     * 토큰 발급 메서드
//     *
//     * @param userId 사용자 ID
//     * @return 토큰 발급 응답
//     */
//    @Override
//    public IssueTokenResponse issueToken(Long userId) {
//        // 사용자 ID로 토큰 생성 및 응답 반환
//        return new IssueTokenResponse(jwtService.createToken(userId));
//    }
//
//    /**
//     * 대기열 추가 메서드
//     *
//     * 첫 진입 시 또는 새로고침 시 호출
//     *
//     * @param userId 사용자 ID
//     * @param token  토큰
//     * @return 활성 상태 확인 응답
//     */
//    @Override
//    public CheckActiveResponse addWaitingQueue(Long userId, String token) {
//        Long waitingNum = null; // 대기번호 초기화
//        Long expectedWaitTimeInSeconds = null; // 예상 대기 시간 초기화
//
//        // 기존 토큰 있으면 만료시킴
//        expiredIfExist(userId);
//
//        // 대기열 활성 유저 수 확인
//        long activeSize = waitingQueueRepository.countByStatusIs(WaitingQueue.Status.ACTIVE);
//        // 활성 유저 수가 최대치보다 적으면 활성화
//        boolean isActive = activeSize < WaitingConstants.ACTIVE_USER_CNT;
//        if (isActive) {
//            // 유저 진입 활성화
//            waitingQueueRepository.save(WaitingQueue.toActiveEntity(userId, token));
//        } else {
//            // 유저 비활성, 대기열 정보 생성
//            waitingNum = waitingQueueRepository.countByStatusIs(WaitingQueue.Status.WAITING);
//            expectedWaitTimeInSeconds = Duration.ofMinutes(waitingNum).toSeconds();
//            waitingQueueRepository.save(WaitingQueue.toWaitingEntity(userId, token));
//        }
//
//        // 활성 상태 확인 응답 생성 및 반환
//        return new CheckActiveResponse(
//                isActive,
//                new CheckActiveResponse.WaitingTicketInfo(waitingNum, expectedWaitTimeInSeconds)
//        );
//    }
//
//    /**
//     * 기존 토큰 만료 메서드
//     *
//     * @param userId 사용자 ID
//     */
//    public void expiredIfExist(Long userId) {
//        // 기존 대기열 정보 조회
//        WaitingQueue existingQueue = waitingQueueRepository.findByUserId(userId);
//        if (existingQueue != null) {
//            // 토큰 만료 처리
//            existingQueue.expiredToken();
//        }
//    }
//
//    /**
//     * 대기열 확인 메서드
//     *
//     * @param userId 사용자 ID
//     * @param token  토큰
//     * @return 활성 상태 확인 응답
//     */
//    @Override
//    @Transactional
//    public CheckActiveResponse checkActive(Long userId, String token) {
//        Long waitingNum = null; // 대기번호 초기화
//        Long expectedWaitTimeInSeconds = null; // 예상 대기 시간 초기화
//
//        // 내 대기 상태 확인
//        WaitingQueue waitingQueue = waitingQueueRepository.findByUserIdAndToken(userId, token);
//        if (waitingQueue == null || waitingQueue.getStatus().equals(WaitingQueue.Status.EXPIRED)) {
//            // 대기열 정보 없거나 만료됨
//            throw new EntityNotFoundException("새로고침하여 다시 진입하세요.");
//        }
//
//        // 활성 여부, 대기열 정보 반환
//        boolean isActive = waitingQueue.getStatus().equals(WaitingQueue.Status.ACTIVE);
//        if (!isActive) {
//            // 대기열 정보 생성
//            waitingNum = waitingQueueRepository.countByRequestTimeBeforeAndStatusIs(WaitingQueue.Status.WAITING, waitingQueue.getRequestTime());
//            expectedWaitTimeInSeconds = Duration.ofMinutes(waitingNum).toSeconds();
//        }
//
//        // 활성 상태 확인 응답 생성 및 반환
//        return new CheckActiveResponse(
//                isActive,
//                new CheckActiveResponse.WaitingTicketInfo(waitingNum, expectedWaitTimeInSeconds)
//        );
//    }
//}

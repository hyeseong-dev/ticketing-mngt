package com.mgnt.ticketing.domain.waiting.service;

import com.mgnt.ticketing.controller.waiting.response.CheckActiveResponse;
import com.mgnt.ticketing.controller.waiting.response.IssueTokenResponse;

/**
 * 대기열 인터페이스
 *
 * 대기열과 관련된 비즈니스 로직을 정의합니다.
 */
public interface WaitingInterface {
    /**
     * 토큰 발급
     *
     * @param userId 사용자 ID
     * @return 토큰 발급 응답
     */
    IssueTokenResponse issueToken(Long userId);

    /**
     * 대기열 저장
     *
     * 첫 진입 시 또는 새로고침 시 호출
     *
     * @param userId 사용자 ID
     * @param token  토큰
     * @return 활성 상태 확인 응답
     */
    CheckActiveResponse addWaitingQueue(Long userId, String token);

    /**
     * 대기열 확인 (활성: 진입 / 비활성: 대기 정보 반환)
     *
     * 대기 시 호출 (polling 방식)
     *
     * @param userId 사용자 ID
     * @param token  토큰
     * @return 활성 상태 확인 응답
     */
    CheckActiveResponse checkActive(Long userId, String token);
}

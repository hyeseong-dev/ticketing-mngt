package com.mgnt.ticketing.controller.waiting.request;

import jakarta.validation.constraints.NotNull;

/**
 * 대기열 추가 요청 DTO
 *
 * @param userId 사용자 ID
 * @param token  인증 토큰
 */
public record AddWaitingQueueRequest(
        @NotNull Long userId,
        @NotNull String token
) {
}

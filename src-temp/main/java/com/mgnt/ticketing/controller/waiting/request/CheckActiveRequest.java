package com.mgnt.ticketing.controller.waiting.request;

import jakarta.validation.constraints.NotNull;

/**
 * 활성 상태 확인 요청 DTO
 *
 * @param userId 사용자 ID
 * @param token  인증 토큰
 */
public record CheckActiveRequest(
        @NotNull Long userId,
        @NotNull String token
) {
}

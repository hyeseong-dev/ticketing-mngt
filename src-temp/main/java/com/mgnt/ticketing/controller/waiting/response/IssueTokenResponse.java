package com.mgnt.ticketing.controller.waiting.response;

/**
 * 토큰 발급 응답 DTO
 *
 * @param token 발급된 토큰
 */
public record IssueTokenResponse(
        String token
) {
}

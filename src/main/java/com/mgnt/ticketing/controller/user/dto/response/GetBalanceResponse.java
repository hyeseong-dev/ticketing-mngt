package com.mgnt.ticketing.controller.user.dto.response;

import com.mgnt.ticketing.domain.user.entity.User;

import java.math.BigDecimal;

/**
 * 사용자 잔액 조회 응답 DTO
 */
public record GetBalanceResponse(
        Long userId,
        BigDecimal balance
) {
    public static GetBalanceResponse from(User user) {
        return new GetBalanceResponse(user.getUserId(), user.getBalance());
    }
}
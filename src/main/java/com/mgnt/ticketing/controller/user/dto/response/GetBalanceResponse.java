package com.mgnt.ticketing.controller.user.dto.response;

import java.math.BigDecimal;

/**
 * 사용자 잔액 조회 응답 DTO
 */
public record GetBalanceResponse(
        BigDecimal balance
) {
}
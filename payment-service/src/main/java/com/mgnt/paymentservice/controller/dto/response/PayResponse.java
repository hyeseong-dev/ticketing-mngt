//package com.mgnt.paymentservice.controller.dto.response;
//
//import com.mgnt.core.enums.ReservationStatus;
//import com.mgnt.paymentservice.domain.entity.Payment;
//import lombok.Builder;
//
//import java.math.BigDecimal;
//
//public record PayResponse(
//        boolean isSuccess,
//        Long paymentId,
//        Long userId,
//        ReservationStatus status,
//        BigDecimal price,
//        BigDecimal balance
//) {
//    @Builder
//    public PayResponse {
//    }
//
//    public static PayResponse from(boolean isSuccess, Long userId, Payment paymentResult, BigDecimal usedBalance) {
//        return PayResponse.builder()
//                .isSuccess(isSuccess)
//                .userId(userId)
//                .paymentId(paymentResult.getPaymentId())
//                .status(paymentResult.getStatus())
//                .price(paymentResult.getPrice())
//                .balance(usedBalance)
//                .build();
//    }
//}
//

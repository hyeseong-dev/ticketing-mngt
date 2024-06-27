package com.mgnt.ticketing.domain.reservation.service.dto;

import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;

/**
 * 예약 및 결제 정보 DTO
 *
 * 이 클래스는 예약 정보와 결제 정보를 함께 전달하기 위한 데이터 전송 객체입니다.
 *
 * @param reservation 예약 정보
 * @param payment 결제 정보
 */
public record GetReservationAndPaymentResDto(
        Reservation reservation,
        Payment payment
) {
}

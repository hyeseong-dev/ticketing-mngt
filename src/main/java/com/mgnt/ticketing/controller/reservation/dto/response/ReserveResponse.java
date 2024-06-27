package com.mgnt.ticketing.controller.reservation.dto.response;

import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * 예약 응답 DTO
 */
public record ReserveResponse(
        Long reservationId,
        Reservation.Status status,
        ConcertInfo concertInfo,
        PaymentInfo paymentInfo
) {

    @Builder
    public ReserveResponse {
    }

    /**
     * Reservation 및 Payment 객체로부터 ReserveResponse 객체를 생성하는 메서드
     *
     * @param reservation 예약 정보
     * @param payment 결제 정보
     * @return ReserveResponse 객체
     */
    public static ReserveResponse from(Reservation reservation, Payment payment) {
        Concert concertInfo = reservation.getConcert();
        ConcertDate concertDateInfo = reservation.getConcertDate();
        Seat seatInfo = reservation.getSeat();

        return ReserveResponse.builder()
                .reservationId(reservation.getReservationId())
                .status(reservation.getStatus())
                .concertInfo(ConcertInfo.builder()
                        .concertId(concertInfo.getConcertId())
                        .concertDateId(concertDateInfo.getConcertDateId())
                        .name(concertInfo.getName())
                        .date(concertDateInfo.getConcertDate())
                        .seatId(seatInfo.getSeatId())
                        .seatNum(seatInfo.getSeatNum())
                        .build())
                .paymentInfo(PaymentInfo.builder()
                        .paymentId(payment.getPaymentId())
                        .status(payment.getStatus())
                        .paymentPrice(payment.getPrice())
                        .build())
                .build();
    }

    @Builder
    public static record ConcertInfo(
            Long concertId,
            Long concertDateId,
            String name,
            ZonedDateTime date,
            Long seatId,
            int seatNum
    ) {
    }

    @Builder
    public static record PaymentInfo(
            Long paymentId,
            Payment.Status status,
            BigDecimal paymentPrice
    ) {
    }
}
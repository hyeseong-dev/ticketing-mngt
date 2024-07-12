//package com.mgnt.userservice.controller.dto.response;
//
//import com.mgnt.core.enums.ReservationStatus;
//import lombok.Builder;
//
//import java.time.ZonedDateTime;
//
//public record GetMyReservationsResponse(
//
//        Long reservationId,
//        ReservationStatus status,
//        ConcertInfo concertInfo
//) {
//    @Builder
//    public GetMyReservationsResponse {
//    }
//
//    public static GetMyReservationsResponse from(GetReservationAndPaymentResDto resDto) {
//        if (resDto == null || resDto.reservation() == null) {
//            return null;
//        }
//
//        Reservation reservation = resDto.reservation();
//        Concert concertInfo = resDto.concert();
//        ConcertDate concertDateInfo = resDto.concertDate();
//        Seat seatInfo = resDto.seat();
//
//        return GetMyReservationsResponse.builder()
//                .reservationId(reservation.getReservationId())
//                .status(reservation.getStatus())
//                .concertInfo(ConcertInfo.builder()
//                        .concertId(concertInfo.getConcertId())
//                        .concertDateId(concertDateInfo.getConcertDateId())
//                        .name(concertInfo.getName())
//                        .date(concertDateInfo.getConcertDate())
//                        .seatId(seatInfo.getSeatId())
//                        .seatNum(seatInfo.getSeatNum())
//                        .build())
//                .build();
//    }
//
//    @Builder
//    public static record ConcertInfo(
//            Long concertId,
//            Long concertDateId,
//            String name,
//            ZonedDateTime date,
//            Long seatId,
//            int seatNum
//    ) {
//    }
//}
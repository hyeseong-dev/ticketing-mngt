//package com.mgnt.reservationservice.domain.service;
//
//import com.mgnt.reservationservice.controller.dto.request.CancelRequest;
//import com.mgnt.reservationservice.controller.dto.request.ReserveRequest;
//import com.mgnt.reservationservice.controller.dto.response.ReserveResponse;
//
//import java.util.List;
//
///**
// * 예약 서비스 인터페이스
// * <p>
// * 이 인터페이스는 예약과 관련된 비즈니스 로직을 정의합니다.
// */
//public interface ReservationInterface {
//
//    /**
//     * 나의 예약 내역 조회
//     *
//     * @param userId 사용자 ID
//     * @return 예약 내역 응답 DTO 리스트
//     */
//    List<ReserveResponse> getMyReservations(Long userId, String userRole);
//
//    /**
//     * 콘서트 좌석 예매
//     *
//     * @param request 예매 요청 DTO
//     * @return 예매 응답 DTO
//     */
//    ReserveResponse reserve(ReserveRequest request);
//
//    /**
//     * 좌석 예매 취소
//     *
//     * @param reservationId 예약 ID
//     * @param request       예매 취소 요청 DTO
//     */
//    void cancel(Long reservationId, CancelRequest request);
//
//}

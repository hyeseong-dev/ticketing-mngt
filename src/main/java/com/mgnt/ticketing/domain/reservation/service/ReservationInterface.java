package com.mgnt.ticketing.domain.reservation.service;

import com.mgnt.ticketing.controller.reservation.dto.request.CancelRequest;
import com.mgnt.ticketing.controller.reservation.dto.request.ReserveRequest;
import com.mgnt.ticketing.controller.reservation.dto.response.ReserveResponse;
import com.mgnt.ticketing.controller.user.dto.response.GetMyReservationsResponse;

import java.util.List;

/**
 * 예약 서비스 인터페이스
 *
 * 이 인터페이스는 예약과 관련된 비즈니스 로직을 정의합니다.
 */
public interface ReservationInterface {

    /**
     * 콘서트 좌석 예매
     *
     * @param request 예매 요청 DTO
     * @return 예매 응답 DTO
     */
    ReserveResponse reserve(ReserveRequest request);

    /**
     * 좌석 예매 취소
     *
     * @param reservationId 예약 ID
     * @param request 예매 취소 요청 DTO
     */
    void cancel(Long reservationId, CancelRequest request);

    /**
     * 나의 예약 내역 조회
     *
     * @param userId 사용자 ID
     * @return 예약 내역 응답 DTO 리스트
     */
    List<GetMyReservationsResponse> getMyReservations(Long userId);
}

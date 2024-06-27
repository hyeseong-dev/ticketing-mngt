package com.mgnt.ticketing.domain.reservation.repository;

import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.reservation.service.dto.GetReservationAndPaymentResDto;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 예약 리포지토리 인터페이스
 *
 * 이 인터페이스는 예약과 관련된 데이터베이스 작업을 처리합니다.
 */
@Repository
public interface ReservationRepository {

    /**
     * 특정 콘서트 날짜 ID에 해당하는 모든 예약 조회
     *
     * @param concertDateId 콘서트 날짜 ID
     * @return 예약 목록
     */
    List<Reservation> findAllByConcertDateId(Long concertDateId);

    /**
     * 특정 콘서트 날짜 ID와 좌석 ID에 해당하는 예약 조회
     *
     * @param concertDateId 콘서트 날짜 ID
     * @param seatId 좌석 ID
     * @return 예약 정보
     */
    Reservation findOneByConcertDateIdAndSeatId(Long concertDateId, Long seatId);

    /**
     * 예약 정보 저장
     *
     * @param reservation 예약 정보
     * @return 저장된 예약 정보
     */
    Reservation save(Reservation reservation);

    /**
     * 특정 예약 ID와 사용자 ID에 해당하는 예약 조회
     *
     * @param reservationId 예약 ID
     * @param userId 사용자 ID
     * @return 예약 정보
     */
    Reservation findByIdAndUserId(Long reservationId, Long userId);

    /**
     * 예약 정보 삭제
     *
     * @param reservation 예약 정보
     */
    void delete(Reservation reservation);

    /**
     * 특정 예약 ID에 해당하는 예약 조회
     *
     * @param reservationId 예약 ID
     * @return 예약 정보
     */
    Reservation findById(Long reservationId);

    /**
     * 특정 사용자 ID에 해당하는 예약 및 결제 정보 조회
     *
     * @param userId 사용자 ID
     * @return 예약 및 결제 정보 목록
     */
    List<GetReservationAndPaymentResDto> getMyReservations(Long userId);
}

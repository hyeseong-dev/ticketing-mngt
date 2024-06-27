package com.mgnt.ticketing.domain.reservation.repository;

import com.mgnt.ticketing.domain.reservation.service.dto.GetReservationAndPaymentResDto;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 예약 JPA 리포지토리 인터페이스
 *
 * 이 인터페이스는 예약과 관련된 데이터베이스 작업을 처리합니다.
 */
public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {

    /**
     * 특정 콘서트 날짜 ID에 해당하는 모든 예약 조회
     *
     * @param concertDateId 콘서트 날짜 ID
     * @return 예약 목록
     */
    List<Reservation> findAllByConcertDate_ConcertDateId(Long concertDateId);

    /**
     * 특정 콘서트 날짜 ID와 좌석 ID에 해당하는 예약 조회
     *
     * @param concertDateId 콘서트 날짜 ID
     * @param seatId 좌석 ID
     * @return 예약 정보
     */
    Reservation findOneByConcertDate_ConcertDateIdAndSeat_SeatId(Long concertDateId, Long seatId);

    /**
     * 특정 예약 ID와 사용자 ID에 해당하는 예약 조회
     *
     * @param reservationId 예약 ID
     * @param userId 사용자 ID
     * @return 예약 정보
     */
    Reservation findByReservationIdAndUser_UserId(Long reservationId, Long userId);

    /**
     * 특정 사용자 ID에 해당하는 예약 및 결제 정보 조회
     *
     * @param userId 사용자 ID
     * @return 예약 및 결제 정보 목록
     */
    @Query("SELECT new com.mgnt.ticketing.domain.reservation.service.dto.GetReservationAndPaymentResDto(r, p)" +
            "FROM Reservation r " +
            "JOIN Payment p on p.reservation.reservationId = r.reservationId " +
            "WHERE r.user.userId = :userId")
    List<GetReservationAndPaymentResDto> getMyReservations(Long userId);
}

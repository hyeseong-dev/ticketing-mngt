package com.mgnt.ticketing.infra_structure;

import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.reservation.repository.ReservationJpaRepository;
import com.mgnt.ticketing.domain.reservation.repository.ReservationRepository;
import com.mgnt.ticketing.domain.reservation.service.dto.GetReservationAndPaymentResDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 예약 리포지토리 구현 클래스
 *
 * 이 클래스는 ReservationRepository 인터페이스를 구현하며,
 * ReservationJpaRepository를 사용하여 예약과 관련된 데이터베이스 작업을 처리합니다.
 */
@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;

    /**
     * 생성자
     *
     * @param reservationJpaRepository ReservationJpaRepository 객체
     */
    public ReservationRepositoryImpl(ReservationJpaRepository reservationJpaRepository) {
        this.reservationJpaRepository = reservationJpaRepository;
    }

    /**
     * 특정 콘서트 날짜 ID에 해당하는 모든 예약 조회
     *
     * @param concertDateId 콘서트 날짜 ID
     * @return 예약 목록
     */
    @Override
    public List<Reservation> findAllByConcertDateId(Long concertDateId) {
        return reservationJpaRepository.findAllByConcertDate_ConcertDateId(concertDateId);
    }

    /**
     * 특정 콘서트 날짜 ID와 좌석 ID에 해당하는 예약 조회
     *
     * @param concertDateId 콘서트 날짜 ID
     * @param seatId 좌석 ID
     * @return 예약 정보
     */
    @Override
    public Reservation findOneByConcertDateIdAndSeatId(Long concertDateId, Long seatId) {
        return reservationJpaRepository.findOneByConcertDate_ConcertDateIdAndSeat_SeatId(concertDateId, seatId);
    }

    /**
     * 예약 정보 저장
     *
     * @param reservation 예약 정보
     * @return 저장된 예약 정보
     */
    @Override
    public Reservation save(Reservation reservation) {
        return reservationJpaRepository.save(reservation);
    }

    /**
     * 특정 예약 ID와 사용자 ID에 해당하는 예약 조회
     *
     * @param reservationId 예약 ID
     * @param userId 사용자 ID
     * @return 예약 정보
     */
    @Override
    public Reservation findByIdAndUserId(Long reservationId, Long userId) {
        return reservationJpaRepository.findByReservationIdAndUser_UserId(reservationId, userId);
    }

    /**
     * 예약 정보 삭제
     *
     * @param reservation 예약 정보
     */
    @Override
    public void delete(Reservation reservation) {
        reservationJpaRepository.delete(reservation);
    }

    /**
     * 특정 예약 ID에 해당하는 예약 조회
     *
     * @param reservationId 예약 ID
     * @return 예약 정보
     * @throws EntityNotFoundException 예약 정보가 없는 경우 예외 발생
     */
    @Override
    public Reservation findById(Long reservationId) {
        return reservationJpaRepository.findById(reservationId).orElseThrow(EntityNotFoundException::new);
    }

    /**
     * 특정 사용자 ID에 해당하는 예약 및 결제 정보 조회
     *
     * @param userId 사용자 ID
     * @return 예약 및 결제 정보 목록
     */
    @Override
    public List<GetReservationAndPaymentResDto> getMyReservations(Long userId) {
        return reservationJpaRepository.getMyReservations(userId);
    }
}

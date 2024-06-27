package com.mgnt.ticketing.domain.concert.service;

import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 콘서트 예약 관리 서비스
 *
 * 이 클래스는 콘서트 예약과 관련된 기능을 제공합니다.
 */
@Component
@RequiredArgsConstructor
public class ConcertReservationManager {

    private final ReservationRepository reservationRepository;

    /**
     * 콘서트 날짜별 예약된 좌석 ID 목록 조회
     *
     * @param concertDateId 콘서트 날짜 ID
     * @return 예약된 좌석 ID 목록
     */
    public List<Long> getReservedSeatIdsByConcertDate(Long concertDateId) {
        // 예약 정보 조회
        List<Reservation> reservations = reservationRepository.findAllByConcertDateId(concertDateId);
        // 예약 완료 및 예약 중인 좌석의 PK 반환
        return reservations.stream()
                .filter(v -> List.of(Reservation.Status.RESERVED, Reservation.Status.ING).contains(v.getStatus()))
                .map(Reservation::getSeat)
                .map(Seat::getSeatId)
                .toList();
    }
}

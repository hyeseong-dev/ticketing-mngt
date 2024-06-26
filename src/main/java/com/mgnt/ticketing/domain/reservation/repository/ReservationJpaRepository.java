package com.mgnt.ticketing.domain.reservation.repository;

import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByConcertDate_ConcertDateId(Long concertDateId);

    Reservation findOneByConcertDate_ConcertDateIdAndSeat_SeatId(Long concertDateId, Long seatId);

    Reservation findByReservationIdAndUser_UserId(Long reservationId, Long userId); // 필드 이름을 userId로 수정
}

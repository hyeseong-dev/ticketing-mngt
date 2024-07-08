package com.mgnt.reservationservice.domain.repository;

import com.mgnt.reservationservice.controller.dto.response.ReserveResponse;
import com.mgnt.reservationservice.domain.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByConcertDateId(Long concertDateId);

    Reservation findByReservationIdAndUserId(Long reservationId, Long userId);

    /*
        TODO: reservation-service에서 concert-service와 kafka로 통신하여 concert-service의 엔티티인 concert, concertDate,
                Seat에 접근하여 데이터를 받아올수 있다. userId는 reservation-service의 컨트롤러에서 http request header정보에서 가져온다.

     */
    @Query("SELECT new com.mgnt.ticketing.domain.reservation.service.dto.GetReservationAndPaymentResDto(r, c, cd, s)" +
            "FROM Reservation r " +
            "JOIN Concert c on c.concertId = r.concertId " +
            "JOIN ConcertDate cd on cd.concertDateId = r.concertDateId " +
            "JOIN Seat s on s.seatNum = r.seatNum " +
            "WHERE r.userId = :userId")
    List<ReserveResponse> getMyReservations(Long userId);

    Reservation findOneByConcertDateIdAndSeatNum(Long concertDateId, int seatNum);
}

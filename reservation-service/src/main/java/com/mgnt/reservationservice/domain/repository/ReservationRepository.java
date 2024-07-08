package com.mgnt.reservationservice.domain.repository;

import com.mgnt.reservationservice.controller.dto.response.ReserveResponse;
import com.mgnt.reservationservice.domain.entity.Reservation;
import com.mgnt.reservationservice.domain.service.dto.GetReservationAndPaymentResDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByConcertDateId(Long concertDateId);

    Reservation findByReservationIdAndUserId(Long reservationId, Long userId);

    Reservation findOneByConcertDateIdAndSeatNum(Long concertDateId, int seatNum);

    @Query("SELECT new com.mgnt.ticketing.domain.reservation.service.dto.GetReservationAndPaymentResDto(r, c, cd, s)" +
            "FROM Reservation r " +
            "JOIN Concert c on c.concertId = r.concertId " +
            "JOIN ConcertDate cd on cd.concertDateId = r.concertDateId " +
            "JOIN Seat s on s.seatNum = r.seatNum " +
            "WHERE r.userId = :userId")
    List<ReserveResponse> getMyReservations(Long userId);

    Reservation save(Reservation reservation);

    Reservation findByIdAndUserId(Long reservationId, Long userId);

    void delete(Reservation reservation);

    Reservation findByReservationId(Long reservationId);

//    List<GetReservationAndPaymentResDto> getMyReservations(Long userId);
}

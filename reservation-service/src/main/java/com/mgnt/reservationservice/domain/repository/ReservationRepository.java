package com.mgnt.reservationservice.domain.repository;

import com.mgnt.reservationservice.controller.dto.response.ReserveResponse;
import com.mgnt.reservationservice.domain.entity.Reservation;
//import com.mgnt.reservationservice.domain.service.dto.GetReservationAndPaymentResDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByConcertDateId(Long concertDateId);

    Reservation findByReservationIdAndUserId(Long reservationId, Long userId);

    Reservation findOneByConcertDateIdAndSeatId(Long concertDateId, Long seatId);

    Reservation save(Reservation reservation);

    Reservation findByReservationId(Long reservationId);

    void delete(Reservation reservation);
}

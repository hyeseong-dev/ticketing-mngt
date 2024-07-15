package com.mgnt.reservationservice.domain.repository;

import com.mgnt.reservationservice.domain.entity.Reservation;
//import com.mgnt.reservationservice.domain.service.dto.GetReservationAndPaymentResDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryCustom {

    List<Reservation> findAllByUserId(Long userId);

    List<Reservation> findAllByConcertDateId(Long concertDateId);

    Reservation findByReservationIdAndUserId(Long reservationId, Long userId);

    Reservation findOneByConcertDateIdAndSeatId(Long concertDateId, Long seatId);

    Reservation save(Reservation reservation);

    void delete(Reservation reservation);
}

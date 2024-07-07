package com.mgnt.reservationservice.domain.repository;

import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.reservation.service.dto.GetReservationAndPaymentResDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository {

    Reservation findOneByConcertDateIdAndSeatNum(Long concertDateId, int seatNum);

    Reservation save(Reservation reservation);

    Reservation findByIdAndUserId(Long reservationId, Long userId);

    void delete(Reservation reservation);

    Reservation findById(Long reservationId);

    List<GetReservationAndPaymentResDto> getMyReservations(Long userId);
}

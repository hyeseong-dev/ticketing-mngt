package com.mgnt.reservationservice.domain.repository;

import com.mgnt.reservationservice.controller.dto.response.ReservationResponseDTO;
import com.mgnt.reservationservice.domain.entity.Reservation;

import java.util.List;

public interface ReservationRedisRepository {
    List<ReservationResponseDTO> getUserReservations(Long userId);

    void saveUserReservations(Long userId, List<ReservationResponseDTO> reservations);

    void removeUserReservations(Long userId);

    void saveReservation(Long UserId, Long reservationId, ReservationResponseDTO reservationInfo);
}
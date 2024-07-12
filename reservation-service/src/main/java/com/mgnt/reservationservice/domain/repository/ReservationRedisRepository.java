package com.mgnt.reservationservice.domain.repository;

import com.mgnt.reservationservice.controller.dto.response.ReservationResponseDTO;

import java.util.List;

public interface ReservationRedisRepository {
    List<ReservationResponseDTO> getUserReservations(Long userId);

    void saveUserReservations(Long userId, List<ReservationResponseDTO> reservations);

    void removeUserReservations(Long userId);
}
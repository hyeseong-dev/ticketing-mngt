package com.mgnt.reservationservice.domain.repository;

import com.mgnt.core.enums.SeatStatus;
import com.mgnt.reservationservice.controller.dto.response.ReservationResponseDTO;
import com.mgnt.reservationservice.domain.entity.Reservation;

import java.util.List;

public interface ReservationRedisRepository {

    Long createIncr(String RESERVATION_INCR_KEY);

    Boolean hasKey(String key);

    void createHash(String key);

    void hSet(String key, String hashKey, String value);

    void updateSeatStatus(Long seatId, SeatStatus newStatus);

    void delete(String key);

    void setex(String key, String value, Long seconds);

    boolean setTempSeat(String key, String value, Long expirationMinutes);

    String get(String key);

    List<ReservationResponseDTO> getUserReservations(Long userId);

    void saveUserReservations(Long userId, List<ReservationResponseDTO> reservations);

    void removeUserReservations(Long userId);

    boolean updateReservationInventory(Long userId, Long reservationId, ReservationResponseDTO reservationInfo);

    Long getAccessTokenTTL(String key);

    boolean setAccessToken(String key, String value, long expirationMinutes);

    void removeFromQueue(String queueKey, String userId);

}
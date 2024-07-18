package com.mgnt.concertservice.domain.repository;

import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.core.enums.SeatStatus;

import java.util.List;
import java.util.Optional;

public interface RedisRepository {

    Optional<Seat> getSeatById(Long seatId);

    void updateSeatStatus(Long seatId, SeatStatus status);

    void setex(String key, long seconds, String value);

    void set(String key, String value);

    String get(String key);

    Long getAccessTokenTTL(String key);

    boolean setAccessToken(String key, String value, long expirationMinutes);

    void removeFromQueue(String queueKey, String userId);

    List<Seat> getCachedSeats(Long concertDateId);

    void cacheSeats(Long concertDateId, List<Seat> seats);

    Optional<Seat> getCachedSeat(Long concertDateId, Long seatId);

    void updateCachedSeatStatus(Long concertDateId, Long seatId, SeatStatus status);
}
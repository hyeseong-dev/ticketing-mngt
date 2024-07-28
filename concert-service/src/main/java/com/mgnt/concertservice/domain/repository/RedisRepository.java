package com.mgnt.concertservice.domain.repository;

import com.mgnt.concertservice.domain.entity.Inventory;
import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.core.enums.SeatStatus;

import java.util.List;
import java.util.Optional;

public interface RedisRepository {

    void saveInventory(Long concertId, Long concertDateId, Inventory inventory);

    Optional<Inventory> getInventory(Long concertId, Long concertDateId);

    boolean updateInventory(Long concertId, Long concertDateId, Long change);

    boolean updateSeatStatus(Long seatId, SeatStatus newStatus);

    Optional<Seat> getSeatById(Long seatId);

    List<Seat> getAllSeats();

    void setex(String key, long seconds, String value);

    void set(String key, String value);

    String get(String key);

    Long getAccessTokenTTL(String key);

    boolean setAccessToken(String key, String value, long expirationMinutes);

    void removeFromQueue(String queueKey, String userId);
    
    Optional<Seat> getCachedSeat(Long seatId);

    List<Seat> getCachedSeats(Long concertDateId);

    void cacheSeats(Long concertDateId, List<Seat> seats);
}
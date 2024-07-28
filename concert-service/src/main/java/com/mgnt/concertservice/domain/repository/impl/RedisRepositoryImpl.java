package com.mgnt.concertservice.domain.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgnt.concertservice.domain.entity.Inventory;
import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.concertservice.domain.repository.RedisRepository;
import com.mgnt.core.enums.SeatStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mgnt.core.constants.Constants.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisRepositoryImpl implements RedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void saveInventory(Long concertId, Long concertDateId, Inventory inventory) {
        String inventoryKey = String.format("%d:%d", concertId, concertDateId);
        try {
            String inventoryJson = objectMapper.writeValueAsString(inventory);
            redisTemplate.opsForHash().put(ALL_INVENTORY_KEY, inventoryKey, inventoryJson);
            log.info("Saved inventory to Redis: concertId={}, concertDateId={}", concertId, concertDateId);
        } catch (Exception e) {
            log.error("Error saving inventory to Redis: concertId={}, concertDateId={}", concertId, concertDateId, e);
            throw new RuntimeException("Error saving inventory to Redis", e);
        }
    }

    public Optional<Inventory> getInventory(Long concertId, Long concertDateId) {
        String inventoryKey = String.format("%d:%d", concertId, concertDateId);
        String inventoryJson = (String) redisTemplate.opsForHash().get(ALL_INVENTORY_KEY, inventoryKey);
        if (inventoryJson == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(inventoryJson, Inventory.class));
        } catch (JsonProcessingException e) {
            log.error("Error processing inventory JSON", e);
            return Optional.empty();
        }
    }

    public boolean updateInventory(Long concertId, Long concertDateId, Long change) {
        String inventoryKey = String.format("%d:%d", concertId, concertDateId);
        try {
            String inventoryJson = (String) redisTemplate.opsForHash().get(ALL_INVENTORY_KEY, inventoryKey);
            if (inventoryJson != null) {
                Inventory inventory = objectMapper.readValue(inventoryJson, Inventory.class);
                if (inventory.getRemaining() + change < 0) {
                    log.warn("Not enough inventory remaining for key: {}", inventoryKey);
                    return false;
                }
                inventory.setRemaining(inventory.getRemaining() + change);
                inventory.setVersion(inventory.getVersion() + 1);
                String updatedInventoryJson = objectMapper.writeValueAsString(inventory);
                redisTemplate.opsForHash().put(ALL_INVENTORY_KEY, inventoryKey, updatedInventoryJson);
                return true;
            } else {
                log.warn("Inventory not found in Redis for key: {}", inventoryKey);
                return false;
            }
        } catch (Exception e) {
            log.error("Error updating inventory in Redis for key: {}", inventoryKey, e);
            return false;
        }
    }

    public boolean updateSeatStatus(Long seatId, SeatStatus newStatus) {
        try {
            String seatKey = SEAT_KEY_PREFIX + seatId;
            String currentSeatJson = redisTemplate.opsForValue().get(seatKey);

            Seat updatedSeat;
            if (currentSeatJson != null) {
                updatedSeat = objectMapper.readValue(currentSeatJson, Seat.class);
                updatedSeat.patchStatus(newStatus);
            } else {
                log.warn("Seat not found in Redis: {}", seatId);
                return false;
            }

            String updatedSeatJson = objectMapper.writeValueAsString(updatedSeat);
            redisTemplate.opsForValue().set(seatKey, updatedSeatJson, CACHE_TTL_SECONDS, TimeUnit.SECONDS);

            redisTemplate.opsForHash().put(ALL_SEATS_KEY, seatId.toString(), updatedSeatJson);

            return true;
        } catch (Exception e) {
            log.error("Error updating seat status for seatId: {}", seatId, e);
            return false;
        }
    }

    public Optional<Seat> getSeatById(Long seatId) {
        String seatJson = (String) redisTemplate.opsForHash().get(ALL_SEATS_KEY, seatId.toString());
        if (seatJson != null) {
            try {
                return Optional.of(objectMapper.readValue(seatJson, Seat.class));
            } catch (JsonProcessingException e) {
                log.error("Error deserializing seat from Redis", e);
            }
        }
        return Optional.empty();
    }

    public List<Seat> getAllSeats() {
        List<Object> jsonSeats = redisTemplate.opsForHash().values(ALL_SEATS_KEY);
        return jsonSeats.stream()
                .map(jsonSeat -> {
                    try {
                        return objectMapper.readValue((String) jsonSeat, Seat.class);
                    } catch (JsonProcessingException e) {
                        log.error("Error deserializing seat from Redis", e);
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void setex(String key, long seconds, String value) {
        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Long getAccessTokenTTL(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public boolean setAccessToken(String key, String value, long expirationMinutes) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, expirationMinutes, TimeUnit.MINUTES);
    }

    public void removeFromQueue(String queueKey, String userId) {
        redisTemplate.opsForZSet().remove(queueKey, userId);
    }

    @Override
    public Optional<Seat> getCachedSeat(Long seatId) {
        return Optional.empty();
    }

    @Override
    public List<Seat> getCachedSeats(Long concertDateId) {
        return List.of();
    }

    @Override
    public void cacheSeats(Long concertDateId, List<Seat> seats) {

    }
}
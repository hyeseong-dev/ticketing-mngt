package com.mgnt.concertservice.domain.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgnt.concertservice.domain.entity.Inventory;
import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.concertservice.domain.repository.RedisRepository;
import com.mgnt.core.enums.SeatStatus;
import com.mgnt.core.util.JsonUtil;
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

    private String getInventoryKey(Long concertId, Long concertDateId) {
        return String.format("%d:%d", concertId, concertDateId);
    }

    @Override
    public void saveInventory(Long concertId, Long concertDateId, Inventory inventory) {
        String inventoryKey = getInventoryKey(concertId, concertDateId);
        try {
            redisTemplate.opsForHash().put(ALL_INVENTORY_KEY, inventoryKey, JsonUtil.convertToJson(inventory));
            log.info("Saved inventory to Redis: concertId={}, concertDateId={}", concertId, concertDateId);
        } catch (Exception e) {
            log.error("Error saving inventory to Redis: concertId={}, concertDateId={}", concertId, concertDateId, e);
            throw new RuntimeException("Error saving inventory to Redis", e);
        }
    }

    @Override
    public Optional<Inventory> getInventory(Long concertId, Long concertDateId) {
        String inventoryKey = getInventoryKey(concertId, concertDateId);
        String inventoryJson = (String) redisTemplate.opsForHash().get(ALL_INVENTORY_KEY, inventoryKey);
        return inventoryJson == null ? Optional.empty() : Optional.of(JsonUtil.convertFromJson(inventoryJson, Inventory.class));
    }

    @Override
    public boolean updateInventory(Long concertId, Long concertDateId, Long change) {
        String inventoryKey = getInventoryKey(concertId, concertDateId);
        try {
            String inventoryJson = (String) redisTemplate.opsForHash().get(ALL_INVENTORY_KEY, inventoryKey);
            if (inventoryJson != null) {
                Inventory inventory = JsonUtil.convertFromJson(inventoryJson, Inventory.class);
                if (inventory.getRemaining() + change < 0) {
                    log.warn("Not enough inventory remaining for key: {}", inventoryKey);
                    return false;
                }
                inventory.setRemaining(inventory.getRemaining() + change);
                inventory.setVersion(inventory.getVersion() + 1);
                redisTemplate.opsForHash().put(ALL_INVENTORY_KEY, inventoryKey, JsonUtil.convertToJson(inventory));
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

    @Override
    public boolean updateSeatStatus(Long seatId, SeatStatus newStatus) {
        try {
            String seatKey = SEAT_KEY_PREFIX + seatId;
            String currentSeatJson = redisTemplate.opsForValue().get(seatKey);

            if (currentSeatJson != null) {
                Seat updatedSeat = JsonUtil.convertFromJson(currentSeatJson, Seat.class);
                updatedSeat.patchStatus(newStatus);
                String updatedSeatJson = JsonUtil.convertToJson(updatedSeat);
                redisTemplate.opsForValue().set(seatKey, updatedSeatJson, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
                redisTemplate.opsForHash().put(ALL_SEATS_KEY, seatId.toString(), updatedSeatJson);
                return true;
            } else {
                log.warn("Seat not found in Redis: {}", seatId);
                return false;
            }
        } catch (Exception e) {
            log.error("Error updating seat status for seatId: {}", seatId, e);
            return false;
        }
    }

    @Override
    public Optional<Seat> getSeatById(Long seatId) {
        String seatJson = (String) redisTemplate.opsForHash().get(ALL_SEATS_KEY, seatId.toString());
        return seatJson == null ? Optional.empty() : Optional.of(JsonUtil.convertFromJson(seatJson, Seat.class));
    }

    @Override
    public List<Seat> getAllSeats() {
        List<Object> jsonSeats = redisTemplate.opsForHash().values(ALL_SEATS_KEY);
        return jsonSeats.stream()
                .map(jsonSeat -> {
                    try {
                        return JsonUtil.convertFromJson((String) jsonSeat, Seat.class);
                    } catch (Exception e) {
                        log.error("Error deserializing seat from Redis", e);
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void setex(String key, long seconds, String value) {
        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

    @Override
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public Long getAccessTokenTTL(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    @Override
    public boolean setAccessToken(String key, String value, long expirationMinutes) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, expirationMinutes, TimeUnit.MINUTES);
    }

    @Override
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
        // Implementation is not provided in the original code
    }
}

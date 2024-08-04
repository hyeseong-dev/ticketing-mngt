package com.mgnt.reservationservice.domain.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.mgnt.core.dto.SeatDTO;
import com.mgnt.core.enums.SeatStatus;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.exception.CustomException;
import com.mgnt.core.util.JsonUtil;
import com.mgnt.reservationservice.controller.dto.response.ReservationResponseDTO;
import com.mgnt.reservationservice.domain.repository.ReservationRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.redisson.api.RMap;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.mgnt.core.constants.Constants.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReservationRedisRepositoryImpl implements ReservationRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String USER_RESERVATIONS_KEY = "user:%d:reservations";

    @Override
    public long countAllReservations() {
        return redisTemplate.opsForHash().size(ALL_RESERVATION_KEY);
    }


    @Override
    public String hGet(String key, String hashKey) {
        return (String) redisTemplate.opsForHash().get(key, hashKey);
    }


    @Override
    public boolean deleteReservation(String key, String hashKey) {
        return redisTemplate.opsForHash().delete(key, hashKey) > 0;
    }

    @Override
    public Long createIncr(String RESERVATION_INCR_KEY) {
        return redisTemplate.opsForValue().increment(RESERVATION_INCR_KEY);
    }

    @Override
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public void createHash(String key) {
        redisTemplate.opsForHash().putIfAbsent(key, "init", "init");
    }

    @Override
    public void hSet(String key, String hashKey, String value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    @Override
    public void updateSeatStatus(Long seatId, SeatStatus newStatus) {
        try {
            String seatKey = SEAT_KEY_PREFIX + seatId;
            String currentSeatJson = hGet(ALL_RESERVATION_KEY, seatId.toString());

            if (currentSeatJson != null) {
                SeatDTO updatedSeat = objectMapper.readValue(currentSeatJson, SeatDTO.class);
                updatedSeat = new SeatDTO(
                        updatedSeat.seatId(),
                        updatedSeat.seatNum(),
                        updatedSeat.price(),
                        newStatus
                );

                String updatedSeatJson = objectMapper.writeValueAsString(updatedSeat);
                redisTemplate.opsForValue().set(seatKey, updatedSeatJson, CACHE_TTL_SECONDS, TimeUnit.SECONDS);

                // ALL_SEATS_KEY 업데이트
                redisTemplate.opsForZSet().add(ALL_SEATS_KEY, updatedSeatJson, seatId);
                redisTemplate.expire(ALL_SEATS_KEY, CACHE_TTL_SECONDS, TimeUnit.SECONDS);

                log.info("Updated seat status in Redis: seatId={}, newStatus={}", seatId, newStatus);
            } else {
                log.warn("Seat not found in Redis: seatId={}", seatId);
            }
        } catch (Exception e) {
            log.error("Error updating seat status for seatId: {}", seatId, e);
            throw new CustomException(ErrorCode.REDIS_OPERATION_FAILED, null, Level.ERROR);
        }
    }

    @Override
    public void delete(String key) {
        redisTemplate.opsForValue().getAndDelete(key);
    }

    @Override
    public void setex(String key, String value, Long seconds) {
        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean setTempSeat(String key, String value, Long expirationMinutes) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, expirationMinutes, TimeUnit.MINUTES);
    }

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public List<ReservationResponseDTO> getUserReservations(Long userId) {
        String key = String.format(USER_RESERVATIONS_KEY, userId);
        String value = redisTemplate.opsForValue().get(key);

        if (value != null) {
            try {
                return objectMapper.readValue(value, new TypeReference<List<ReservationResponseDTO>>() {
                });
            } catch (JsonProcessingException e) {
                log.error("Failed to parse reservation info from Redis: userId={}", userId, e);
                throw new CustomException(ErrorCode.REDIS_OPERATION_FAILED, null, Level.ERROR);
            }
        }
        return null;
    }

    @Override
    public void saveUserReservations(Long userId, List<ReservationResponseDTO> reservations) {
        String key = String.format(USER_RESERVATIONS_KEY, userId);
        try {
            String value = objectMapper.writeValueAsString(reservations);
            redisTemplate.opsForValue().set(key, value);
            log.info("Saved reservations to Redis: userId={}, count={}", userId, reservations.size());
        } catch (JsonProcessingException e) {
            log.error("Failed to save reservation info to Redis: userId={}", userId, e);
            throw new CustomException(ErrorCode.REDIS_OPERATION_FAILED, null, Level.ERROR);
        }
    }

    @Override
    public void removeUserReservations(Long userId) {
        String key = String.format(USER_RESERVATIONS_KEY, userId);
        redisTemplate.delete(key);
        log.info("Removed reservations from Redis: userId={}", userId);
    }

    @Override
    public boolean updateReservationInventory(Long userId, Long reservationId, ReservationResponseDTO reservationInfo) {
        try {
            redisTemplate.opsForHash().put(
                    ALL_RESERVATION_KEY,
                    reservationId.toString(),
                    JsonUtil.convertToJson(reservationInfo)
            );
            log.info("Updated reservation in Redis: userId={}, reservationId={}", userId, reservationId);
            return true;
        } catch (Exception e) {
            log.error("Failed to update reservation in Redis: userId={}, reservationId={}", userId, reservationId, e);
            return false;
        }
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

    private SeatDTO getSeatFromRedis(Long seatId) {
        Set<String> seatSet = redisTemplate.opsForZSet().rangeByScore(ALL_SEATS_KEY, seatId, seatId);
        if (seatSet.isEmpty()) {
            throw new CustomException(ErrorCode.SEAT_NOT_FOUND, null, Level.WARN);
        }
        try {
            return objectMapper.readValue(seatSet.iterator().next(), SeatDTO.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse seat info from Redis: seatId={}", seatId, e);
            throw new CustomException(ErrorCode.REDIS_OPERATION_FAILED, null, Level.ERROR);
        }
    }
}
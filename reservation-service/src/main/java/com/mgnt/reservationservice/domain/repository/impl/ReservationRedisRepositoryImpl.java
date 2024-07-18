package com.mgnt.reservationservice.domain.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.mgnt.reservationservice.controller.dto.response.ReservationResponseDTO;
import com.mgnt.reservationservice.domain.repository.ReservationRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReservationRedisRepositoryImpl implements ReservationRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String USER_RESERVATIONS_KEY = "user:%d:reservations";

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
        }
    }

    @Override
    public void removeUserReservations(Long userId) {
        String key = String.format(USER_RESERVATIONS_KEY, userId);
        redisTemplate.delete(key);
        log.info("Removed reservations from Redis: userId={}", userId);
    }

    @Override
    public void saveReservation(Long userId, Long reservationId, ReservationResponseDTO reservationInfo) {
        try {
            String key = String.format("user:%d:reservation:%d", userId, reservationId);
            String value = objectMapper.writeValueAsString(reservationInfo);
            redisTemplate.opsForValue().set(key, value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to save reservation info to Redis", e);
        }
    }
}
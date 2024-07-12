package com.mgnt.reservationservice.domain.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.mgnt.reservationservice.controller.dto.response.ReservationResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReservationRedisRepositoryImpl implements ReservationRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String USER_RESERVATIONS_KEY = "user:%d:reservations";

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
}
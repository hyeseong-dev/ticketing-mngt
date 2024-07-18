package com.mgnt.concertservice.domain.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.concertservice.domain.repository.RedisRepository;
import com.mgnt.concertservice.domain.repository.SeatRepository;
import com.mgnt.core.enums.SeatStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisRepositoryImpl implements RedisRepository {

    private final SeatRepository seatRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String ALL_SEATS_KEY = "all_seats";
    private static final long CACHE_TTL_SECONDS = 3600; // 1시간

    @Transactional
    public void updateSeatStatus(Long seatId, SeatStatus newStatus) {
        // MySQL 업데이트
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new EntityNotFoundException("Seat not found"));
        seat.patchStatus(newStatus);
        seatRepository.save(seat);

        // Redis 업데이트
        try {
            String updatedJsonSeat = objectMapper.writeValueAsString(seat);
            redisTemplate.opsForZSet().add(ALL_SEATS_KEY, updatedJsonSeat, seatId);
            redisTemplate.expire(ALL_SEATS_KEY, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.error("Error updating seat in Redis", e);
        }
    }

    public Optional<Seat> getSeatById(Long seatId) {
        // Redis에서 먼저 조회
        Optional<Seat> redisSeat = getSeatById(seatId);

        if (redisSeat.isPresent()) {
            return redisSeat;
        }

        // Redis에 없으면 MySQL에서 조회 후 Redis에 저장
        Optional<Seat> mySqlSeat = seatRepository.findById(seatId);
        mySqlSeat.ifPresent(seat -> {
            try {
                String jsonSeat = objectMapper.writeValueAsString(seat);
                redisTemplate.opsForZSet().add(ALL_SEATS_KEY, jsonSeat, seatId);
                redisTemplate.expire(ALL_SEATS_KEY, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            } catch (JsonProcessingException e) {
                log.error("Error adding seat to Redis", e);
            }
        });

        return mySqlSeat;
    }

    public List<Seat> getAllSeats() {
        // Redis에서 모든 좌석 조회
        Set<String> jsonSeats = redisTemplate.opsForZSet().range(ALL_SEATS_KEY, 0, -1);

        if (jsonSeats != null && !jsonSeats.isEmpty()) {
            return jsonSeats.stream()
                    .map(jsonSeat -> {
                        try {
                            return objectMapper.readValue(jsonSeat, Seat.class);
                        } catch (JsonProcessingException e) {
                            log.error("Error deserializing seat from Redis", e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        // Redis에 데이터가 없으면 MySQL에서 조회 후 Redis에 저장
        List<Seat> seats = seatRepository.findAll();
        seats.forEach(seat -> {
            try {
                String jsonSeat = objectMapper.writeValueAsString(seat);
                redisTemplate.opsForZSet().add(ALL_SEATS_KEY, jsonSeat, seat.getSeatId());
            } catch (JsonProcessingException e) {
                log.error("Error adding seat to Redis", e);
            }
        });
        redisTemplate.expire(ALL_SEATS_KEY, CACHE_TTL_SECONDS, TimeUnit.SECONDS);

        return seats;
    }

    @Override
    public void setex(String key, long seconds, String value) {
        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

    @Override
    public void set(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.info("Redis에 좌석 상태 저장: key={}, value={}", key, value);
        } catch (Exception e) {
            log.error("Redis에 좌석 상태 저장 실패: key={}, value={}, error={}", key, value, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public String get(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            log.info("Redis에서 좌석 상태 조회: key={}, value={}", key, value);
            return value;
        } catch (Exception e) {
            log.error("Redis에서 좌석 상태 조회 실패: key={}, error={}", key, e.getMessage(), e);
            throw e;
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

    @Override
    public List<Seat> getCachedSeats(Long concertDateId) {
        String key = "seats:" + concertDateId;
        List<String> seatJsonList = redisTemplate.opsForList().range(key, 0, -1);
        return seatJsonList.stream()
                .map(this::deserializeSeat)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public void cacheSeats(Long concertDateId, List<Seat> seats) {
        String key = "seats:" + concertDateId;
        List<String> seatJsonList = seats.stream()
                .map(this::serializeSeat)
                .collect(Collectors.toList());
        redisTemplate.opsForList().rightPushAll(key, seatJsonList);
        redisTemplate.expire(key, 1, TimeUnit.HOURS); // Cache for 1 hour
    }

    @Override
    public Optional<Seat> getCachedSeat(Long concertDateId, Long seatId) {
        String key = "seat:" + concertDateId + ":" + seatId;
        String seatJson = redisTemplate.opsForValue().get(key);
        return deserializeSeat(seatJson);
    }

    @Override
    public void updateCachedSeatStatus(Long concertDateId, Long seatId, SeatStatus status) {
        String key = "seat:" + concertDateId + ":" + seatId;
        Optional<Seat> seatOpt = getCachedSeat(concertDateId, seatId);
        seatOpt.ifPresent(seat -> {
            seat.patchStatus(status);
            String updatedSeatJson = serializeSeat(seat);
            redisTemplate.opsForValue().set(key, updatedSeatJson);
        });
    }

    private String serializeSeat(Seat seat) {
        try {
            return objectMapper.writeValueAsString(seat);
        } catch (JsonProcessingException e) {
            log.error("Error serializing seat: {}", e.getMessage());
            return null;
        }
    }

    private Optional<Seat> deserializeSeat(String seatJson) {
        try {
            return Optional.ofNullable(objectMapper.readValue(seatJson, Seat.class));
        } catch (JsonProcessingException e) {
            log.error("Error deserializing seat: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
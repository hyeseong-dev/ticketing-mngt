package com.mgnt.reservationservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgnt.core.util.JsonUtil;
import com.mgnt.reservationservice.domain.entity.Reservation;
import com.mgnt.reservationservice.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mgnt.core.constants.Constants.*;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ReservationRepository reservationRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        initializeReservation();
    }

    private void initializeReservation() throws Exception {
        // Redis에서 기존의 모든 예약 키 삭제
        redisTemplate.delete(ALL_RESERVATION_KEY);
        redisTemplate.delete(RESERVATION_INCR_KEY);

        // MySQL에서 모든 예약 정보 가져오기
        List<Reservation> allReservations = reservationRepository.findAll();

        // 예약 정보를 맵으로 변환 (key: 예약 ID, value: 예약 정보 JSON 문자열)
        Map<String, String> reservationMap = allReservations.stream()
                .collect(Collectors.toMap(
                        reservation -> reservation.getReservationId().toString(),
                        JsonUtil::convertToJson
                ));

        // 예약 정보를 Redis에 저장
        if (!reservationMap.isEmpty()) {
            redisTemplate.opsForHash().putAll(ALL_RESERVATION_KEY, reservationMap);
        }

        // 가장 큰 reservationId를 찾아서 Redis의 next_id로 설정 (없으면 0으로 초기화)
        Long maxReservationId = allReservations.stream()
                .mapToLong(Reservation::getReservationId)
                .max()
                .orElse(0L);
        redisTemplate.opsForValue().set(RESERVATION_INCR_KEY, String.valueOf(maxReservationId));

        // 해시 테이블의 초기화가 완료된 후 기록된 예약 수 확인
        long reservationCount = redisTemplate.opsForHash().size(ALL_RESERVATION_KEY);
        System.out.println("Loaded " + reservationCount + " reservation records into Redis.");
    }
}

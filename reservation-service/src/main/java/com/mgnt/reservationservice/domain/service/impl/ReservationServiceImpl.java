package com.mgnt.reservationservice.domain.service.impl;

import com.mgnt.core.enums.ReservationStatus;
import com.mgnt.core.enums.SeatStatus;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.event.concert_service.InventoryReservationRequestEvent;
import com.mgnt.core.event.concert_service.SeatStatusUpdatedEvent;
import com.mgnt.core.event.reservation_service.QueueEventStatus;
import com.mgnt.core.event.reservation_service.ReservationInventoryCreateResponseDTO;
import com.mgnt.core.exception.CustomException;
import com.mgnt.core.util.JsonUtil;
import com.mgnt.reservationservice.controller.dto.request.ReservationRequest;
import com.mgnt.reservationservice.controller.dto.request.TokenRequestDTO;
import com.mgnt.reservationservice.controller.dto.response.ReservationResponseDTO;
import com.mgnt.reservationservice.controller.dto.response.TokenResponseDTO;
import com.mgnt.reservationservice.domain.entity.Reservation;
import com.mgnt.reservationservice.domain.repository.QueueRedisRepository;
import com.mgnt.reservationservice.domain.repository.ReservationRedisRepository;
import com.mgnt.reservationservice.domain.repository.ReservationRepository;
import com.mgnt.reservationservice.domain.service.ReservationService;
import com.mgnt.reservationservice.domain.service.ReservationValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mgnt.core.constants.Constants.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final RedissonClient redissonClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ReservationRepository reservationRepository;
    private final ReservationValidator reservationValidator;
    private final ReservationRedisRepository reservationRedisRepository;
    private final QueueRedisRepository queueRedisRepository;
    private final TransactionTemplate transactionTemplate;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Long TEMP_RESERVATION_SECONDS = 300L; // 5분
    private final Long TEMP_RESERVATION_MINUTES = 5L; // 5분

    @Override
    public void handleTempReservationExpiration(String expiredKey) {
        try {
            String[] keyParts = expiredKey.split(":");
            if (keyParts.length != 3) {
                log.error("Invalid key format: {}", expiredKey);
                return;
            }

            Long seatId = Long.parseLong(keyParts[2]);
            String lockKey = "lock:seat:" + seatId;
            RLock lock = redissonClient.getLock(lockKey);

            try {
                boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
                if (!isLocked) {
                    log.warn("Failed to acquire lock for seat {} expiration handling", seatId);
                    return;
                }

                String seatKey = "seat:" + seatId;
                String seatInfo = reservationRedisRepository.get(seatKey);
                if (seatInfo == null) {
                    log.warn("Seat information not found in Redis for key: {}", seatKey);
                    return;
                }

                // 좌석 상태를 AVAILABLE로 변경
                reservationRedisRepository.updateSeatStatus(seatId, SeatStatus.AVAILABLE);

                // Kafka 이벤트 발행
                SeatStatusUpdatedEvent event = new SeatStatusUpdatedEvent(
                        null, null, null, null, seatId, null, SeatStatus.AVAILABLE
                );
                kafkaTemplate.send(TOPIC_SEAT_STATUS_UPDATES, event);

                log.info("Temporary reservation expired and seat status reset for seatId: {}", seatId);

                // 여기에 예약 만료에 대한 추가 로직 (예: 관련 예약 정보 삭제) 을 구현할 수 있습니다.
                // 예를 들어, 임시 예약 정보를 Redis에서 삭제
                String tempReservationKey = "temp_reservation:" + seatId;
                reservationRedisRepository.delete(tempReservationKey);

            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            log.error("Error handling temporary reservation expiration", e);
        }
    }

    @Override
    public TokenResponseDTO getTokenStatus(Long userId, TokenRequestDTO request) {
        String tokenKey = String.format(ACCESS_TOKEN_KEY, userId, request.concertId(), request.concertDateId());
        String token = queueRedisRepository.getAccessToken(tokenKey);
        Long ttlSeconds = queueRedisRepository.getAccessTokenTTL(tokenKey);

        log.info("Checking token status for user {}. Token: {}, TTL: {}", userId, token, ttlSeconds);
        ZonedDateTime expiryTime = null;
        if (ttlSeconds != null && ttlSeconds > 0) {
            expiryTime = ZonedDateTime.now().plus(ttlSeconds, ChronoUnit.SECONDS);
        }

        if (token != null) {
            return new TokenResponseDTO(userId, request.concertId(), request.concertDateId(),
                    QueueEventStatus.READY, null, token, expiryTime);
        } else {
            return new TokenResponseDTO(userId, request.concertId(), request.concertDateId(),
                    QueueEventStatus.WAITING, null, null, null);
        }
    }

    public List<ReservationResponseDTO> getMyReservations(Long userId) {
        // 먼저 Redis에서 캐시된 데이터 조회
        List<ReservationResponseDTO> cachedReservations = reservationRedisRepository.getUserReservations(userId);

        if (cachedReservations != null && !cachedReservations.isEmpty()) {
            log.info("캐시에서 예약 정보 조회: userId={}, count={}", userId, cachedReservations.size());
            return cachedReservations;
        }

        // 캐시에 데이터가 없으면 DB에서 조회
        List<Reservation> reservations = reservationRepository.findAllByUserId(userId);
        log.info("DB에서 예약 정보 조회: userId={}, count={}", userId, reservations.size());

        List<ReservationResponseDTO> responseDTOs = reservations.stream()
                .map(ReservationResponseDTO::from)
                .collect(Collectors.toList());

        // 조회한 데이터를 Redis에 캐시
        reservationRedisRepository.saveUserReservations(userId, responseDTOs);

        return responseDTOs;
    }


    private ReservationResponseDTO convertToDTO(Reservation reservation) {
        return new ReservationResponseDTO(
                reservation.getReservationId(),
                reservation.getStatus(),
                reservation.getUserId(),
                reservation.getConcertId(),
                reservation.getConcertDateId(),
                reservation.getSeatId(),
                reservation.getPrice(),
                reservation.getReservedAt()
        );
    }

    @Transactional
    public void updateReservation(Long reservationId, ReservationStatus newStatus) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));

        reservation.updateStatus(newStatus);
        reservationRepository.save(reservation);

        // 캐시 업데이트
        Long userId = reservation.getUserId();
        List<ReservationResponseDTO> userReservations = reservationRedisRepository.getUserReservations(userId);

        if (userReservations != null) {
            List<ReservationResponseDTO> updatedReservations = userReservations.stream()
                    .map(dto -> dto.reservationId().equals(reservationId) ? convertToDTO(reservation) : dto)
                    .collect(Collectors.toList());

            reservationRedisRepository.saveUserReservations(userId, updatedReservations);
        }
    }

    @Override
    public ReservationInventoryCreateResponseDTO createReservationWithoutPayment(Long userId, ReservationRequest request) {
        try {

            // Redis에서 고유 ID 생성
            Long reservationId = reservationRedisRepository.createIncr(RESERVATION_INCR_KEY);

            // 예약 정보 객체 생성
            Reservation reservation = request.toEntity(userId);
            reservation.updateReservationId(reservationId);
            reservation.setCreatedAt(ZonedDateTime.now());
            reservation.setUpdatedAt(ZonedDateTime.now());

            // 예약 정보를 JSON으로 직렬화하여 Redis에 저장
            String reservationJson = JsonUtil.convertToJson(reservation);
            reservationRedisRepository.hSet(ALL_RESERVATION_KEY, reservationId.toString(), reservationJson);

            // 비동기로 MySQL에 예약 정보 저장 및 Redis 롤백 처리
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                transactionTemplate.execute(status -> {
                    try {
                        reservationRepository.save(reservation);
                    } catch (Exception e) {
                        // MySQL 저장 실패 시 Redis 롤백 처리
                        reservationRedisRepository.deleteReservation(ALL_RESERVATION_KEY, reservationId.toString());
                        status.setRollbackOnly();
                        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), Level.ERROR);
                    }
                    return null;
                });
            }, executorService);

            // 비동기 작업이 완료된 후 kafka 이벤트 발행
            future.thenRun(() -> {
                // Kafka 이벤트 발행
                kafkaTemplate.send(TOPIC_INVENTORY_RESERVATION_REQUESTS, new InventoryReservationRequestEvent(
                        reservation.getReservationId(),
                        request.concertId(),
                        request.concertDateId(),
                        true
                ));
            }).exceptionally(error -> {
                // 예외 처리 로직
                log.error("Error occurred while saving reservation to MySQL or sending Kafka event", error);
                throw new CustomException(ErrorCode.RESERVATION_FAILED, error.getMessage(), Level.ERROR);
            });


            return new ReservationInventoryCreateResponseDTO(
                    reservation.getReservationId(),
                    reservation.getUserId(),
                    reservation.getConcertId(),
                    reservation.getConcertDateId(),
                    reservation.getSeatId(),
                    reservation.getPrice(),
                    reservation.getCreatedAt(),
                    reservation.getExpiresAt()
            );
        } catch (Exception e) {
            throw new CustomException(ErrorCode.RESERVATION_FAILED, e.getMessage(), Level.ERROR);
        }
    }

}
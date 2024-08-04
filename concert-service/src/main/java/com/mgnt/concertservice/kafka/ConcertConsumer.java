package com.mgnt.concertservice.kafka;

import com.mgnt.concertservice.domain.entity.*;
import com.mgnt.concertservice.domain.repository.*;
import com.mgnt.concertservice.domain.service.ConcertService;
import com.mgnt.core.constants.Constants;
import com.mgnt.core.dto.SeatDTO;
import com.mgnt.core.enums.SeatStatus;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.event.concert_service.*;
import com.mgnt.core.event.reservation_service.ReservationConfirmedEvent;
import com.mgnt.core.event.reservation_service.ReservationFailedEvent;
import com.mgnt.core.event.reservation_service.ReservationRequestedEvent;
import com.mgnt.core.exception.CustomException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.mgnt.core.constants.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConcertConsumer {

    private final InventoryRepository inventoryRepository;
    private final ConcertDateRepository concertDateRepository;
    private final PlaceRepository placeRepository;
    private final ConcertRepository concertRepository;
    private final SeatRepository seatRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisRepository redisRepository;
    private final RedissonClient redissonClient;

    private final RedisTemplate redisTemplate;


    @KafkaListener(topics = TOPIC_SEAT_RESERVATION_REQUESTS)
    @Transactional
    public void handleSeatReservationRequest(SeatStatusUpdatedEvent event) {
        log.info("Received inventory reservation request: {}", event);
        String inventoryKey = String.format("%d:%d", event.concertId(), event.concertDateId());
        String lockKey = "lock:seat:" + event.seatId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("Failed to acquire lock for seat {}", event.seatId());
                sendFailureResponse(event);
                return;
            }

            Seat seat = redisRepository.getSeatById(event.seatId()).orElseThrow(
                    () -> new CustomException(ErrorCode.SEAT_NOT_FOUND, null, Level.WARN));

            if (seat.getStatus() == SeatStatus.AVAILABLE) {
                redisRepository.updateSeatStatus(event.seatId(), SeatStatus.TEMP_RESERVED);

                // 임시 예약 키 설정
                String tempReservationKey = String.format(TEMP_RESERVATION_KEY, event.seatId());
                redisRepository.setex(tempReservationKey, TEMP_RESERVATION_SECONDS, event.userId().toString());

                // 만료 키 설정
                String expiryKey = String.format(EXPIRY_KEY, event.seatId());
                redisRepository.setex(expiryKey, TEMP_RESERVATION_SECONDS, event.userId().toString());

                log.info("Successfully reserved seat: {}", event.seatId());
                sendSuccessResponse(event, seat.getPrice());
            } else {
                log.warn("Seat {} is not available for reservation", event.seatId());
                sendFailureResponse(event);
            }
        } catch (Exception e) {
            log.error("Error processing seat reservation request", e);
            SeatReservationResponseEvent failureEvent = new SeatReservationResponseEvent(
                    event.reservationId(),
                    false,
                    null
            );
            kafkaTemplate.send(TOPIC_SEAT_RESERVATION_RESPONSES, failureEvent);
            log.info("Sent seat reservation failure response: {}", failureEvent);
        }
    }

    private void sendSuccessResponse(SeatStatusUpdatedEvent event, BigDecimal price) {
        SeatReservationResponseEvent responseEvent = new SeatReservationResponseEvent(
                event.reservationId(),
                true,
                price
        );
        kafkaTemplate.send(TOPIC_SEAT_RESERVATION_RESPONSES, responseEvent);
        log.info("Sent seat reservation success response: {}", responseEvent);
    }

    private void sendFailureResponse(SeatStatusUpdatedEvent event) {
        SeatReservationResponseEvent failureEvent = new SeatReservationResponseEvent(
                event.reservationId(),
                false,
                null
        );
        kafkaTemplate.send(TOPIC_SEAT_RESERVATION_RESPONSES, failureEvent);
        log.info("Sent seat reservation failure response: {}", failureEvent);
    }

    @KafkaListener(topics = TOPIC_CONCERT_INFO_REQUESTS)
    public void handleConcertInfoRequest(ConcertInfoRequestEvent event) {
        log.info("Received concert info request: {}", event);
        try {
            Concert concert = concertRepository.findByConcertId(event.concertId())
                    .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND, "Concert not found", Level.WARN));

            Place place = placeRepository.findByPlaceId(concert.getPlaceId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND, "Place not found", Level.WARN));

            ConcertDate concertDate = concertDateRepository.findByConcertDateId(event.concertDateId())
                    .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_DATE_NOT_FOUND, "Concert date not found", Level.WARN));

            Seat seat = seatRepository.findByConsertDateIdAndSeatId(event.concertDateId(), event.seatId())
                    .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, "Seat not found", Level.WARN));

            ConcertInfoDTO concertInfo = new ConcertInfoDTO(
                    concert.getConcertId(),
                    concert.getName(),
                    new PlaceDTO(place.getPlaceId(), place.getName(), place.getSeatsCnt()),
                    new ConcertDateDTO(concertDate.getConcertDateId(), concertDate.getConcertDate()),
                    new SeatDTO(seat.getSeatId(), seat.getSeatNum(), seat.getPrice(), seat.getStatus())
            );

            ConcertInfoResponseEvent responseEvent = new ConcertInfoResponseEvent(
                    event.reservationId(),
                    concertInfo
            );

            kafkaTemplate.send(TOPIC_CONCERT_INFO_RESPONSES, responseEvent);
            log.info("Sent concert info response: {}", responseEvent);
        } catch (Exception e) {
            log.error("Error processing concert info request", e);
            // 에러 응답 이벤트 전송
//            ErrorResponseEvent errorEvent = new ErrorResponseEvent(event.reservationId(), e.getMessage());
//            kafkaTemplate.send(TOPIC_CONCERT_INFO_RESPONSES, errorEvent);
            log.info("Sent error response for concert info request: {}", e.toString());
        }
    }

    @KafkaListener(topics = TOPIC_RESERVATION_REQUESTS)
    @Transactional
    public void handleReservationRequest(ReservationRequestedEvent event) {
        log.info("Received reservation request: {}", event);
        String lockKey = "lock:seat:" + event.seatId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 락 획득 시도
            boolean isLocked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("Failed to acquire lock for seat {}", event.seatId());
                sendReservationFailureNotification(event);
                return;
            }
            // 좌석 상태 확인 및 업데이트
            Seat seat = redisRepository.getSeatById(event.seatId()).orElseThrow(() ->
                    new CustomException(ErrorCode.SEAT_NOT_FOUND, "Seat not found", Level.WARN));

            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new CustomException(ErrorCode.SEAT_NOT_AVAILABLE, "Seat not available", Level.WARN);
            }

            // 좌석 상태를 TEMP_RESERVED로 업데이트
            boolean updateSuccess = redisRepository.updateSeatStatus(event.seatId(), SeatStatus.TEMP_RESERVED);
            if (!updateSuccess) {
                throw new CustomException(ErrorCode.SEAT_UPDATE_FAILED, "Failed to update seat status", Level.ERROR);
            }

            // 임시 예약 키 설정
            String tempReservationKey = String.format(Constants.TEMP_RESERVATION_KEY, event.seatId());
            redisRepository.setex(tempReservationKey, TEMP_RESERVATION_SECONDS, event.userId().toString());

            // 만료 키 설정
            String expiryKey = String.format(Constants.EXPIRY_KEY, event.seatId());
            redisRepository.setex(expiryKey, TEMP_RESERVATION_SECONDS, event.userId().toString());

            // 좌석 상태 업데이트 이벤트 발행
            SeatStatusUpdatedEvent updateEvent = new SeatStatusUpdatedEvent(
                    null, event.userId(), event.concertId(), event.concertDateId(), event.seatId(), seat.getPrice(), SeatStatus.TEMP_RESERVED);
            kafkaTemplate.send(TOPIC_SEAT_STATUS_UPDATES, updateEvent);

            log.info("Temporary reservation set successfully for user {} for seat {}", event.userId(), event.seatId());

        } catch (CustomException e) {
            log.error("Error handling reservation request", e);
            handleReservationError(e, event);
        } catch (Exception e) {
            log.error("Unexpected error during reservation process", e);
            handleReservationError(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage(), Level.ERROR), event);
        }
    }

    private void handleReservationError(CustomException e, ReservationRequestedEvent event) {
        log.warn("Reservation process exception: {}", e.getMessage());
        // 에러 발생 시 좌석 상태를 AVAILABLE로 복구
        redisRepository.updateSeatStatus(event.seatId(), SeatStatus.AVAILABLE);
        sendReservationFailureNotification(event);
    }

    private void sendReservationFailureNotification(ReservationRequestedEvent event) {
//        ReservationFailedEvent failedEvent = new ReservationFailedEvent(
//                event.userId(), event.concertId(), event.concertDateId(), event.seatId());
//        kafkaTemplate.send(TOPIC_RESERVATION_FAILED, failedEvent);
//        log.info("Sent reservation failure notification: {}", failedEvent);
    }

    private void handleReservationError(Exception e, ReservationRequestedEvent event) {
        if (e instanceof CustomException) {
            log.warn("Reservation process exception: {}", e.getMessage());
        } else if (e instanceof OptimisticLockingFailureException) {
            log.warn("Concurrent modification detected: {}", e.getMessage());
        } else {
            log.error("Unexpected error during reservation process", e);
        }
        sendReservationFailureNotification(event);
    }

    @KafkaListener(topics = TOPIC_RESERVATION_FAILED)
    @Transactional
    public void handleReservationFailed(ReservationFailedEvent event) {
        log.info("Handling reservation failed event: {}", event);
        try {
            redisRepository.updateSeatStatus(event.seatId(), SeatStatus.AVAILABLE);
            log.info("Reset seat status to AVAILABLE: seatId={}", event.seatId());
        } catch (Exception e) {
            log.error("Error handling reservation failed event", e);
        }
    }

    @KafkaListener(topics = TOPIC_RESERVATION_CONFIRMED)
    @Transactional
    public void handleReservationConfirmed(ReservationConfirmedEvent event) {
        log.info("Handling reservation confirmed event: {}", event);
        redisRepository.updateSeatStatus(event.seatId(), event.status());
        log.info("Updated seat status: seatId={}, status={}", event.seatId(), event.status());
    }

    @KafkaListener(topics = TOPIC_INVENTORY_RESERVATION_REQUESTS)
    @Transactional
    public void handleInventoryReservationRequest(InventoryReservationRequestEvent event) {
        log.info("Received inventory reservation request: {}", event);
        try {
            // MySQL 인벤토리 업데이트
            boolean mysqlSuccess = updateInventoryRemainingOptimisticLock(event.concertId(), event.concertDateId(), -1L);
            // Redis 인벤토리 업데이트
            boolean redisSuccess = redisRepository.updateInventory(event.concertId(), event.concertDateId(), -1L);
            boolean isSuccess = mysqlSuccess && redisSuccess;

            if (!isSuccess) {
                // MySQL과 Redis 업데이트 중 하나라도 실패했다면 롤백
                if (mysqlSuccess) {
                    updateInventoryRemainingOptimisticLock(event.concertId(), event.concertDateId(), 1L);
                }
                if (redisSuccess) {
                    redisRepository.updateInventory(event.concertId(), event.concertDateId(), 1L);
                }
            }

            InventoryReservationResponseEvent responseEvent = new InventoryReservationResponseEvent(
                    event.reservationId(),
                    event.concertId(),
                    event.concertDateId(),
                    isSuccess
            );

            kafkaTemplate.send(TOPIC_INVENTORY_RESERVATION_RESPONSES, responseEvent);
            log.info("Sent inventory reservation response: {}", responseEvent);
        } catch (CustomException e) {
            log.warn("Inventory reservation failed: {}", e.getMessage());
            sendInventoryReservationFailureResponse(event, e);
        } catch (Exception e) {
            log.error("Error handling inventory reservation request", e);
            sendInventoryReservationFailureResponse(event, e);
        }
    }

    private void sendInventoryReservationFailureResponse(InventoryReservationRequestEvent event, Exception e) {
        InventoryReservationResponseEvent failureEvent = new InventoryReservationResponseEvent(
                event.reservationId(),
                event.concertId(),
                event.concertDateId(),
                false
        );
        kafkaTemplate.send(TOPIC_INVENTORY_RESERVATION_RESPONSES, failureEvent);
        log.info("Sent inventory reservation failure response: {}", failureEvent);
    }

    public boolean updateInventoryRemainingOptimisticLock(Long concertId, Long concertDateId, Long remainingChange) {
        int maxRetries = 3;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                Inventory inventory = inventoryRepository.findByConcertIdAndConcertDateId(concertId, concertDateId)
                        .orElseThrow(() -> new CustomException(ErrorCode.INVENTORY_NOT_FOUND, "Inventory not found", Level.WARN));

                Long updatedRows = inventoryRepository.updateRemainingInventoryWithOptimisticLock(concertId, concertDateId, remainingChange, inventory.getVersion());
                if (updatedRows > 0) {
                    log.info("Successfully updated inventory: concertId={}, concertDateId={}, change={}", concertId, concertDateId, remainingChange);
                    return true;
                } else {
                    log.warn("Inventory update failed due to concurrent modification. Retrying... (Attempt {})", attempt + 1);
                }
            } catch (OptimisticLockException e) {
                log.warn("Optimistic lock exception occurred. Retrying... (Attempt {})", attempt + 1);
                if (attempt == maxRetries - 1) {
                    log.error("Failed to update inventory after {} attempts", maxRetries);
                    throw new CustomException(ErrorCode.CONCURRENT_MODIFICATION, "Too many concurrent modifications", Level.ERROR);
                }
            }
        }
        return false;
    }
}
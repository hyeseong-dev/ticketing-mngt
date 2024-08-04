package com.mgnt.reservationservice.kafka;

import com.mgnt.core.enums.ReservationStatus;
import com.mgnt.core.enums.SeatStatus;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.event.concert_service.InventoryReservationRequestEvent;
import com.mgnt.core.event.concert_service.InventoryReservationResponseEvent;
import com.mgnt.core.event.concert_service.SeatStatusUpdatedEvent;
import com.mgnt.core.event.payment_service.PaymentCompletedEvent;
import com.mgnt.core.event.reservation_service.ReservationConfirmedEvent;
import com.mgnt.core.event.reservation_service.ReservationCreatedEvent;
import com.mgnt.core.event.reservation_service.ReservationFailedEvent;
import com.mgnt.core.event.reservation_service.ReservationInventoryCreateResponseDTO;
import com.mgnt.core.exception.CustomException;
import com.mgnt.core.util.JsonUtil;
import com.mgnt.reservationservice.controller.dto.request.ReservationInventoryRequest;
import com.mgnt.reservationservice.controller.dto.request.ReservationRequest;
import com.mgnt.reservationservice.controller.dto.response.ReservationResponseDTO;
import com.mgnt.reservationservice.domain.entity.Reservation;
import com.mgnt.reservationservice.domain.repository.ReservationRedisRepository;
import com.mgnt.reservationservice.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.mgnt.core.constants.Constants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationConsumer {

    private final ReservationRepository reservationRepository;
    private final ReservationRedisRepository reservationRedisRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final Long TEMP_RESERVATION_SECONDS = 300L; // 5분
    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;

    // Kafka 토픽 이름을 상수로 정의
    private static final String TOPIC_SEAT_STATUS_UPDATES = "seat-status-updates";
    private static final String TOPIC_RESERVATIONS_CREATED = "reservations-created";
    private static final String TOPIC_RESERVATION_FAILED = "reservation-failed";
    private static final String TOPIC_PAYMENT_COMPLETED = "payment-completed";
    private static final String TOPIC_RESERVATION_CONFIRMED = "reservation-confirmed";
    private static final String TOPIC_INVENTORY_RESERVATION_RESPONSES = "inventory-reservation-responses";

    @KafkaListener(topics = TOPIC_SEAT_STATUS_UPDATES)
    @Transactional
    public void handleSeatStatusUpdate(SeatStatusUpdatedEvent event) {
        log.info("Received seat status update event: {}", event);
        try {
            Reservation reservation = Reservation.builder()
                    .userId(event.userId())
                    .concertId(event.concertId())
                    .concertDateId(event.concertDateId())
                    .seatId(event.seatId())
                    .status(ReservationStatus.ING)
                    .price(event.price())
                    .reservedAt(ZonedDateTime.now())
                    .build();
            reservationRepository.save(reservation);
            log.info("Created new reservation: {}", reservation);

            // Redis의 임시 예약 정보 업데이트
            String reservationKey = String.format("temp_reservation:%d:%d:%d", event.userId(), event.concertId(), event.seatId());
            reservationRedisRepository.setex(reservationKey, "CONFIRMED", TEMP_RESERVATION_SECONDS);
            log.info("Updated temporary reservation in Redis: key={}", reservationKey);

            ReservationCreatedEvent createdEvent = new ReservationCreatedEvent(
                    reservation.getReservationId(), reservation.getUserId(), reservation.getPrice());
            kafkaTemplate.send(TOPIC_RESERVATIONS_CREATED, createdEvent);
            log.info("Sent reservation created event: {}", createdEvent);

        } catch (Exception e) {
            log.error("Error handling seat status update", e);
            ReservationFailedEvent failedEvent = new ReservationFailedEvent(
                    event.userId(), event.reservationId(), event.concertDateId(), event.seatId(), e.getMessage());
            kafkaTemplate.send(TOPIC_RESERVATION_FAILED, failedEvent);
            log.info("Sent reservation failed event: {}", failedEvent);
        }
    }

    @KafkaListener(topics = TOPIC_PAYMENT_COMPLETED)
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Received payment completed event: {}", event);
        try {
            Reservation reservation = reservationRepository.findById(event.reservationId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND, "Reservation not found", Level.WARN));

            if (event.isSuccess()) {
                reservation.updateStatus(ReservationStatus.RESERVED);
                reservationRepository.save(reservation);
                log.info("Updated reservation status to RESERVED: {}", reservation);

                ReservationConfirmedEvent confirmedEvent = new ReservationConfirmedEvent(
                        reservation.getReservationId(), reservation.getConcertDateId(),
                        reservation.getSeatId(), SeatStatus.RESERVED);
                kafkaTemplate.send(TOPIC_RESERVATION_CONFIRMED, confirmedEvent);
                log.info("Sent reservation confirmed event: {}", confirmedEvent);
            } else {
                reservation.updateStatus(ReservationStatus.CANCEL);
                reservationRepository.save(reservation);
                String failMessage = "Updated reservation status to CANCEL: ".concat(reservation.getReservationId().toString());
                log.info(failMessage);
                ReservationFailedEvent failedEvent = new ReservationFailedEvent(
                        event.userId(), event.reservationId(), reservation.getConcertDateId(), reservation.getSeatId(), failMessage);
                kafkaTemplate.send(TOPIC_RESERVATION_FAILED, failedEvent);
                log.info("Sent reservation failed event: {}", failedEvent);
            }
        } catch (Exception e) {
            log.error("Error handling payment completed", e);
            ReservationFailedEvent failedEvent = new ReservationFailedEvent(
                    event.userId(), event.reservationId(), null, null, null);
            kafkaTemplate.send(TOPIC_RESERVATION_FAILED, failedEvent);
            log.info("Sent reservation failed event due to error: {}", failedEvent);
        }
    }

    @KafkaListener(topics = TOPIC_INVENTORY_RESERVATION_RESPONSES)
    @Transactional
    public void handleInventoryReservationResponse(InventoryReservationResponseEvent event) {
        log.info("Received inventory reservation response event: {}", event);
        try {
            Reservation reservation = reservationRepository.findByReservationId(event.reservationId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND, "Reservation not found", Level.INFO));

            if (event.isSuccess()) {
                reservation.updateStatus(ReservationStatus.RESERVED);
                log.info("Updated reservation status to RESERVED: {}", reservation);
            } else {
                reservation.updateStatus(ReservationStatus.CANCEL);
                log.info("Updated reservation status to CANCEL: {}", reservation);
            }

            reservationRepository.save(reservation);

            // Redis 캐시 업데이트
            ReservationResponseDTO updatedDto = ReservationResponseDTO.from(reservation);
            reservationRedisRepository.updateReservationInventory(reservation.getUserId(), reservation.getReservationId(), updatedDto);
            log.info("Updated reservation in Redis cache: userId={}, reservationId={}", reservation.getUserId(), reservation.getReservationId());

            // 클라이언트에게 결과 통지 (예: WebSocket 또는 SSE를 통해)
            notifyClient(reservation.getUserId(), updatedDto);
            log.info("Notified client about reservation update: userId={}", reservation.getUserId());

        } catch (Exception e) {
            log.error("Error handling inventory reservation response", e);
            throw new CustomException(ErrorCode.RESERVATION_FAILED, "Failed to process inventory reservation response", Level.ERROR);
        }
    }

    private void notifyClient(Long userId, ReservationResponseDTO reservationResponseDTO) {
        // WebSocket 또는 SSE를 통한 클라이언트 통지 로직
        log.info("Notifying client: userId={}, reservationDetails={}", userId, reservationResponseDTO);
    }

    @KafkaListener(topics = RESERVATION_INVENTORY_TOPIC)
    public void processReservation(ReservationInventoryRequest request) {
        log.info("Received reservation request: {}", request);

        String lockKey = RESERVATION_INVENTORY_LOCK_PREFIX + request.seatId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!isLocked) {
                log.error("Failed to acquire lock for seat {}", request.seatId());
                return;
            }

            ReservationInventoryCreateResponseDTO response = transactionTemplate.execute(status -> {
                try {
                    Long reservationId = reservationRedisRepository.createIncr(RESERVATION_INCR_KEY);
                    Reservation reservation = request.toEntity(request.userId());
                    reservation.updateReservationId(reservationId);
                    reservation.initializeTimestamps();

                    String reservationJson = JsonUtil.convertToJson(reservation);
                    reservationRedisRepository.hSet(ALL_RESERVATION_KEY, reservationId.toString(), reservationJson);
                    reservationRepository.save(reservation);

                    // Kafka 이벤트 발행 (동기적으로 처리)
                    try {
                        kafkaTemplate.send(TOPIC_INVENTORY_RESERVATION_REQUESTS, new InventoryReservationRequestEvent(
                                reservation.getReservationId(),
                                request.concertId(),
                                request.concertDateId(),
                                true
                        )).get(); // .get()을 호출하여 동기적으로 처리
                        log.info("Kafka message sent successfully for reservationId: {}", reservation.getReservationId());
                    } catch (Exception e) {
                        log.error("Failed to send Kafka message", e);
                        status.setRollbackOnly();
                        return null;
                    }

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
                    status.setRollbackOnly();
                    log.error("Reservation failed", e);
                    return null;
                }
            });

            if (response != null) {
                log.info("Reservation created successfully: {}", response.reservationId());
            } else {
                log.error("Failed to create reservation");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Reservation process was interrupted", e);
        } catch (Exception e) {
            log.error("Error processing reservation", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
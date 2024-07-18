package com.mgnt.concertservice.kafka;

import com.mgnt.concertservice.domain.entity.*;
import com.mgnt.concertservice.domain.repository.*;
import com.mgnt.concertservice.domain.service.ConcertService;
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
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
    private final ConcertService concertService;
    private final RedisRepository redisRepository;

    // Kafka 토픽 이름을 상수로 정의
    private static final String TOPIC_SEAT_RESERVATION_REQUESTS = "seat-reservation-requests";
    private static final String TOPIC_SEAT_RESERVATION_RESPONSES = "seat-reservation-responses";
    private static final String TOPIC_CONCERT_INFO_REQUESTS = "concert-info-requests";
    private static final String TOPIC_CONCERT_INFO_RESPONSES = "concert-info-responses";
    private static final String TOPIC_RESERVATION_REQUESTS = "reservation-requests";
    private static final String TOPIC_SEAT_STATUS_UPDATES = "seat-status-updates";
    private static final String TOPIC_RESERVATION_FAILED = "reservation-failed";
    private static final String TOPIC_RESERVATION_CONFIRMED = "reservation-confirmed";
    private static final String TOPIC_INVENTORY_RESERVATION_REQUESTS = "inventory-reservation-requests";
    private static final String TOPIC_INVENTORY_RESERVATION_RESPONSES = "inventory-reservation-responses";

    @KafkaListener(topics = TOPIC_SEAT_RESERVATION_REQUESTS)
    @Transactional
    public void handleSeatReservationRequest(SeatStatusUpdatedEvent event) {
        log.info("Received seat reservation request: {}", event);
        try {
            Seat seat = redisRepository.getSeatById(event.seatId()).orElseThrow(
                    () -> new CustomException(ErrorCode.SEAT_NOT_FOUND, "Seat not found", Level.WARN));

            boolean success = false;
            if (seat.getStatus() == SeatStatus.AVAILABLE) {
                redisRepository.updateSeatStatus(event.seatId(), SeatStatus.RESERVED);
                success = true;
                log.info("Successfully reserved seat: {}", event.seatId());
            } else {
                log.warn("Seat {} is not available for reservation", event.seatId());
            }

            SeatReservationResponseEvent responseEvent = new SeatReservationResponseEvent(
                    event.reservationId(),
                    success,
                    seat.getPrice()
            );
            kafkaTemplate.send(TOPIC_SEAT_RESERVATION_RESPONSES, responseEvent);
            log.info("Sent seat reservation response: {}", responseEvent);
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

        try {
            Optional<Seat> seatOpt = redisRepository.getSeatById(event.seatId());
            if (seatOpt.isEmpty()) {
                log.warn("Seat not found: seatId={}", event.seatId());
                throw new CustomException(ErrorCode.SEAT_NOT_FOUND, "Seat not found", Level.WARN);
            }

            Seat seat = seatOpt.get();
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                log.warn("Seat not available: seatId={}, status={}", event.seatId(), seat.getStatus());
                throw new CustomException(ErrorCode.SEAT_NOT_AVAILABLE, "Seat not available", Level.WARN);
            }

            redisRepository.updateSeatStatus(seat.getSeatId(), SeatStatus.RESERVED);
            log.info("Updated seat status to RESERVED: seatId={}", seat.getSeatId());
            sendSeatStatusUpdate(seat, event);
        } catch (Exception e) {
            log.error("Error handling reservation request", e);
            handleReservationError(e, event);
        }
    }

    private void sendSeatStatusUpdate(Seat seat, ReservationRequestedEvent event) {
        SeatStatusUpdatedEvent updateEvent = new SeatStatusUpdatedEvent(
                null, event.userId(), event.concertId(), event.concertDateId(),
                event.seatId(), seat.getPrice()
        );
        kafkaTemplate.send(TOPIC_SEAT_STATUS_UPDATES, updateEvent)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully sent seat status update: {}", updateEvent);
                    } else {
                        log.error("Failed to send seat status update: {}", ex.toString());
                    }
                });
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

    private void sendReservationFailureNotification(ReservationRequestedEvent event) {
//        ReservationFailedEvent failedEvent = new ReservationFailedEvent(
//                event.userId(), event.concertId(), event.concertDateId(), event.seatId());
//        kafkaTemplate.send(TOPIC_RESERVATION_FAILED, failedEvent);
        log.info("Sent reservation failure notification: {}", event.toString());
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
            boolean isSuccess = updateInventoryRemainingOptimisticLock(event.concertId(), event.concertDateId(), -1L);
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
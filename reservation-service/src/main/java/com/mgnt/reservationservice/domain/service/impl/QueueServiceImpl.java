package com.mgnt.reservationservice.domain.service.impl;

import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.event.reservation_service.*;
import com.mgnt.core.exception.CustomException;
import com.mgnt.reservationservice.domain.repository.QueueRedisRepository;
import com.mgnt.reservationservice.domain.service.QueueService;
import com.mgnt.reservationservice.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

import static com.mgnt.core.constants.Constants.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    public static final String QUEUE_PROCESS = "queue-process";
    public static final String QUEUE_EVENTS = "queue-events";
    private final JwtUtil jwtUtil;
    private final QueueRedisRepository queueRedisRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void removeFromQueue(Long userId, QueueEntryRequest request) {
        String queueKey = generateQueueKey(request.concertId(), request.concertDateId());
        queueRedisRepository.removeFromQueue(queueKey, userId.toString());
    }

    @Override
    public QueueEntryResponse enterQueue(Long userId, QueueEntryRequest request) {
        String queueKey = generateQueueKey(request.concertId(), request.concertDateId());
        Long position = queueRedisRepository.addToQueue(queueKey, userId.toString());

        log.info("User {} entered queue for concert {} on date {}. Position: {}",
                userId, request.concertId(), request.concertDateId(), position);

        if (position == null) {
            log.error("Failed to add user {} to queue", userId);
            throw new CustomException(ErrorCode.QUEUE_ENTRY_FAILED, null, Level.ERROR);
        }

        QueueEvent event = new QueueEvent(userId, request.concertId(), request.concertDateId(),
                QueueEventType.QUEUE_ENTRY, QueueEventStatus.WAITING, position);
        kafkaTemplate.send(QUEUE_EVENTS, event);
        kafkaTemplate.send(QUEUE_PROCESS, event);

        log.info("Queue entry event sent for user {}", userId);

        return new QueueEntryResponse(userId, position, request.concertId(), request.concertDateId());
    }

    public QueueStatusResponse getQueueStatus(Long userId, QueueEntryRequest request) {
        String queueKey = generateQueueKey(request.concertId(), request.concertDateId());
        Long position = queueRedisRepository.getQueuePosition(queueKey, userId.toString());

        if (position != null) {
            return new QueueStatusResponse(userId, request.concertId(), request.concertDateId(),
                    QueueEventStatus.WAITING, position);
        } else {
            // 대기열에 없는 경우, 토큰의 존재 여부를 확인하여 READY 상태인지 확인
            String tokenKey = String.format(ACCESS_TOKEN_KEY, userId, request.concertId(), request.concertDateId());
            String token = queueRedisRepository.getAccessToken(tokenKey);

            if (token != null) {
                return new QueueStatusResponse(userId, request.concertId(), request.concertDateId(),
                        QueueEventStatus.READY, null);
            } else {
                return new QueueStatusResponse(userId, request.concertId(), request.concertDateId(),
                        QueueEventStatus.NOT_IN_QUEUE, null);
            }
        }
    }


    private String generateQueueKey(Long concertId, Long concertDateId) {
        return String.format(WAITING_QUEUE_KEY, concertId, concertDateId);
    }

    @KafkaListener(topics = QUEUE_PROCESS)
    public void handleEventQueue(QueueEvent event) {
        processQueue(event);
    }

    @Override
    @Transactional
    public void processQueue(QueueEvent event) {
        String queueKey = generateQueueKey(event.concertId(), event.concertDateId());
        log.info("Processing queue for concert {} on date {}", event.concertId(), event.concertDateId());

        for (int i = 0; i < BATCH_SIZE; i++) {
            Set<String> userIds = queueRedisRepository.getTopUsers(queueKey, 1);
            if (userIds.isEmpty()) {
                log.info("Queue is empty. Stopping process.");
                break;
            }
            Long userId = Long.valueOf(userIds.iterator().next());
            log.info("Processing user {} from queue", userId);

            try {
                allowAccessToReservationPage(userId, event.concertId(), event.concertDateId());
                queueRedisRepository.removeFromQueue(queueKey, userId.toString());
                log.info("User {} processed and removed from queue", userId);
            } catch (CustomException e) {
                log.error("Error processing user {}: {}", userId, e.getMessage());
                queueRedisRepository.removeFromQueue(queueKey, userId.toString());
            }
        }
    }

    @Transactional
    public void allowAccessToReservationPage(Long userId, Long concertId, Long concertDateId) {
        log.info("Allowing access to reservation page for user: {}, concert: {}, date: {}",
                userId, concertId, concertDateId);

        try {
            String accessToken = jwtUtil.createToken(userId, concertId, concertDateId);
            String tokenKey = String.format(ACCESS_TOKEN_KEY, userId, concertId, concertDateId);
            String countKey = String.format(ATTEMPT_COUNT_KEY, userId, concertId, concertDateId);

            // 이미 존재하는 토큰 확인
            String existingToken = queueRedisRepository.getAccessToken(tokenKey);
            if (existingToken != null) {
                log.info("Access token already exists for user: {}. Token: {}", userId, existingToken);
                throw new CustomException(ErrorCode.RESERVATION_TOKEN_ALREADY_EXIST, null, Level.WARN);
            }

            boolean tokenSet = queueRedisRepository.setAccessToken(tokenKey, accessToken, ACCESS_TOKEN_EXPIRATION);
            boolean countSet = queueRedisRepository.setAttemptCount(countKey, 0, ATTEMPT_COUNT_EXPIRATION);

            log.info("Access token set: {}, Attempt count set: {} for user {}", tokenSet, countSet, userId);

            if (tokenSet && countSet) {
                log.info("Access granted for user: {}. Access token: {}", userId, accessToken);

                ReservationAccessGrantedEvent event = new ReservationAccessGrantedEvent(userId, concertId, concertDateId, accessToken);
                kafkaTemplate.send("reservation-access-granted", event);

                log.info("Reservation access granted event sent for user: {}", userId);
            } else {
                String errorMessage = "Failed to set access token or attempt count";
                log.warn("{} for user: {}", errorMessage, userId);
                throw new CustomException(ErrorCode.RESERVATION_ACCESS_FAILED, errorMessage, Level.WARN);
            }
        } catch (Exception e) {
            String errorMessage = "Error while allowing access to reservation page";
            log.error("{} for user: {}", errorMessage, userId, e);
            throw new CustomException(ErrorCode.RESERVATION_ACCESS_FAILED, errorMessage, Level.ERROR);
        }
    }


    private String generateAccessToken() {
        return UUID.randomUUID().toString();
    }
}

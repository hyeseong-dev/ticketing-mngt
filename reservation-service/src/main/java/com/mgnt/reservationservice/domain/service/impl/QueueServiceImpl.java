package com.mgnt.reservationservice.domain.service.impl;

import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.event.reservation_service.*;
import com.mgnt.core.exception.CustomException;
import com.mgnt.reservationservice.domain.repository.QueueRedisRepository;
import com.mgnt.reservationservice.domain.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    private final QueueRedisRepository queueRedisRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String WAITING_QUEUE_KEY = "queue:%d:%d";
    private final int BATCH_SIZE = 10;
    private static final int ACCESS_TOKEN_EXPIRATION = 10; // 10 minutes
    private static final int ATTEMPT_COUNT_EXPIRATION = 24; // 24 hours
    private static final String ACCESS_TOKEN_KEY = "reservation:access:%d:%d:%d"; // userId:concertId:concertDateId
    private static final String ATTEMPT_COUNT_KEY = "reservation:attempts:%d:%d:%d";


    @Override
    public void removeFromQueue(Long userId, QueueEntryRequest request) {
        String queueKey = generateQueueKey(request.concertId(), request.concertDateId());
        queueRedisRepository.removeFromQueue(queueKey, userId.toString());
    }

    @Override
    public QueueEntryResponse enterQueue(Long userId, QueueEntryRequest request) {
        String queueKey = generateQueueKey(request.concertId(), request.concertDateId());
        Long position = queueRedisRepository.addToQueue(queueKey, userId.toString());

        if (position == null) {
            throw new CustomException(ErrorCode.QUEUE_ENTRY_FAILED, null, Level.ERROR);
        }

        QueueEvent event = new QueueEvent(userId,
                request.concertId(),
                request.concertDateId(),
                QueueEventType.QUEUE_ENTRY,
                QueueEventStatus.WAITING,
                position);
        kafkaTemplate.send("queue-events", event);

        // queue-process 토픽에 이벤트 발행
        kafkaTemplate.send("queue-process", event);


        return new QueueEntryResponse(userId, position, request.concertId(), request.concertDateId());
    }

    @Override
    public QueueStatusResponse getQueueStatus(Long userId, QueueEntryRequest request) {
        String queueKey = generateQueueKey(request.concertId(), request.concertDateId());
        Long position = queueRedisRepository.getQueuePosition(queueKey, userId.toString());

        if (position != null) {
            if (position == 0) {
                String tokenKey = String.format(ACCESS_TOKEN_KEY, userId, request.concertId(), request.concertDateId());
                String token = queueRedisRepository.getAccessToken(tokenKey);
                return new QueueStatusResponse(userId, request.concertId(), request.concertDateId(), QueueEventStatus.READY, 0L, token);
            } else {
                return new QueueStatusResponse(userId,
                        request.concertId(),
                        request.concertDateId(),
                        QueueEventStatus.WAITING,
                        position,
                        null);
            }
        } else {
            return new QueueStatusResponse(userId,
                    request.concertId(),
                    request.concertDateId(),
                    QueueEventStatus.NOT_IN_QUEUE,
                    null,
                    null);
        }
    }

    private String generateQueueKey(Long concertId, Long concertDateId) {
        return String.format(WAITING_QUEUE_KEY, concertId, concertDateId);
    }

    @KafkaListener(topics = "queue-process")
    public void handleEventQueue(QueueEvent event) {
        processQueue(event.concertId(), event.concertDateId());
    }

    @Override
    @Transactional
    public void processQueue(Long concertId, Long concertDateId) {
        String queueKey = generateQueueKey(concertId, concertDateId);
        for (int i = 0; i < BATCH_SIZE; i++) {
            Long userId = queueRedisRepository.popUserFromQueue(queueKey);
            if (userId == null) {
                break; // 대기열이 비어있으면 처리 중단
            }
            // 예매 페이지 접속 허용 로직
            allowAccessToReservationPage(userId, concertId, concertDateId);
        }
    }

    @Transactional
    public void allowAccessToReservationPage(Long userId, Long concertId, Long concertDateId) {
        log.info("Allowing access to reservation page for user: {}, concert: {}, date: {}", userId, concertId, concertDateId);

        try {
            String accessToken = generateAccessToken();
            String tokenKey = String.format(ACCESS_TOKEN_KEY, userId, concertId, concertDateId);
            String countKey = String.format(ATTEMPT_COUNT_KEY, userId, concertId, concertDateId);

            boolean tokenSet = queueRedisRepository.setAccessToken(tokenKey, accessToken, ACCESS_TOKEN_EXPIRATION);
            boolean countSet = queueRedisRepository.setAttemptCount(countKey, 0, ATTEMPT_COUNT_EXPIRATION);

            if (tokenSet && countSet) {
                log.info("Access granted for user: {}. Access token: {}", userId, accessToken);

                ReservationAccessGrantedEvent event = new ReservationAccessGrantedEvent(userId, concertId, concertDateId, accessToken);
                kafkaTemplate.send("reservation-access-granted", event);

                log.info("Notification sent to user: {}. Access token: {}", userId, accessToken);
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

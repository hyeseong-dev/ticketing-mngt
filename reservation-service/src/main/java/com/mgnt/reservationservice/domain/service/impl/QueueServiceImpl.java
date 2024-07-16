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

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    private final QueueRedisRepository queueRedisRepository;
    private final KafkaTemplate<String, QueueEvent> kafkaTemplate;
    private final String WAITING_QUEUE_KEY = "queue:%d:%d";
    private final int BATCH_SIZE = 10;

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
        QueueEventStatus status = (position != null) ? QueueEventStatus.WAITING : QueueEventStatus.NOT_IN_QUEUE;

        // 대기열 변경 이벤트 발행
        QueueEvent event = new QueueEvent(userId,
                request.concertId(),
                request.concertDateId(),
                QueueEventType.QUEUE_UPDATE,
                status, position);

        kafkaTemplate.send("queue-events", event);
        return new QueueStatusResponse(userId, request.concertId(), request.concertDateId(), status, position);
    }

    private String generateQueueKey(Long concertId, Long concertDateId) {
        return String.format(WAITING_QUEUE_KEY, concertId, concertDateId);
    }

    @KafkaListener(topics = "queue-process")
    public void handleEventQueue(QueueEvent event) {
        processQueue(event.concertId(), event.concertDateId());
    }

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

    private void allowAccessToReservationPage(Long userId, Long concertId, Long concertDateId) {
        // 예매 페이지 접속 허용 로직
        log.info("User {} allowed to access the reservation page for concert {} on date {}.", userId, concertId, concertDateId);
    }
}

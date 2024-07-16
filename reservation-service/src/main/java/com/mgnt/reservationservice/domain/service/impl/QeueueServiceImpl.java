package com.mgnt.reservationservice.domain.service.impl;

import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.event.reservation_service.QueueEntryRequest;
import com.mgnt.core.event.reservation_service.QueueEntryResponse;
import com.mgnt.core.event.reservation_service.QueueStatusResponse;
import com.mgnt.core.exception.CustomException;
import com.mgnt.reservationservice.domain.repository.QueueRedisRepository;
import com.mgnt.reservationservice.domain.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QeueueServiceImpl implements QueueService {

    private final QueueRedisRepository queueRedisRepository;
    private final String WAITING_QUEUE_KEY = "queue:%d:%d";

    @Override
    public QueueEntryResponse enterQueue(Long userId, QueueEntryRequest request) {
        String queueKey = generateQueueKey(request.concertId(), request.concertDateId());
        Long position = queueRedisRepository.addToQueue(queueKey, userId.toString());

        if (position == null) {
            throw new CustomException(ErrorCode.QUEUE_ENTRY_FAILED, null, Level.ERROR);
        }

        return new QueueEntryResponse(userId, position, request.concertId(), request.concertDateId());
    }

    @Override
    public QueueStatusResponse getQueueStatus(Long userId, QueueEntryRequest request) {
        String queueKey = generateQueueKey(request.concertId(), request.concertDateId());
        Long position = queueRedisRepository.getQueuePosition(queueKey, userId.toString());
        String status = (position != null) ? "WAITING" : "NOT_IN_QUEUE";
        return new QueueStatusResponse(userId, request.concertId(), request.concertDateId(), status, position);
    }

    @Override
    public void removeFromQueue(Long userId, QueueEntryRequest request) {
        String queueKey = generateQueueKey(request.concertId(), request.concertDateId());
        queueRedisRepository.removeFromQueue(queueKey, userId.toString());
    }


    private String generateQueueKey(Long concertId, Long concertDateId) {
        return String.format(WAITING_QUEUE_KEY, concertId, concertDateId);
    }


}
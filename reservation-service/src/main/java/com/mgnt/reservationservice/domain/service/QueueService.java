package com.mgnt.reservationservice.domain.service;

import com.mgnt.core.event.reservation_service.QueueEntryRequest;
import com.mgnt.core.event.reservation_service.QueueEntryResponse;
import com.mgnt.core.event.reservation_service.QueueStatusResponse;

public interface QueueService {
    QueueEntryResponse enterQueue(Long userId, QueueEntryRequest request);

    QueueStatusResponse getQueueStatus(Long userId, QueueEntryRequest request);

    void removeFromQueue(Long userId, QueueEntryRequest request);

    void processQueue(Long concertId, Long concertDateId);
}
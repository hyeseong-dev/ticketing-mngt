package com.mgnt.core.event.reservation_service;

public record QueueEvent(
        Long userId,
        Long concertId,
        Long concertDateId,
        QueueEventType eventType, // 예: QUEUE_ENTRY, QUEUE_UPDATE, RESERVATION_SUCCESS, RESERVATION_FAILURE
        QueueEventStatus eventStatus, // 예: WAITING, PROCESSING, SUCCESS, FAILURE, NOT_IN_QUEUE
        Long position
) {
}

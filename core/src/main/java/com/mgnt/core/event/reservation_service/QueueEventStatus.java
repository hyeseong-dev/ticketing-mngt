package com.mgnt.core.event.reservation_service;

public enum QueueEventStatus {
    NOT_IN_QUEUE,
    WAITING,
    READY,
    PROCESSING,
    SUCCESS,
    FAILURE
}

package com.mgnt.core.event;

public record ReservationConfirmedEvent(Long reservationId) implements Event {
}
package com.mgnt.core.event;

public record ReservationCreatedEvent(Long reservationId,
                                      Long userId,
                                      Long concertId,
                                      Long concertDateId,
                                      int seatNum) implements Event {
}
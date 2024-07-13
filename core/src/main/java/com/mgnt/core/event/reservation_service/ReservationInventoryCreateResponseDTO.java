package com.mgnt.core.event.reservation_service;

import java.time.ZonedDateTime;

public record ReservationInventoryCreateResponseDTO(Long reservationId,
                                                    Long userId,
                                                    Long concertId,
                                                    Long concertDateId,
                                                    Long seatId,
                                                    ZonedDateTime createdAt) {
}

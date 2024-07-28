package com.mgnt.core.event.reservation_service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record ReservationInventoryCreateResponseDTO(Long reservationId,
                                                    Long userId,
                                                    Long concertId,
                                                    Long concertDateId,
                                                    Long seatId,
                                                    BigDecimal price,
                                                    ZonedDateTime createdAt,
                                                    ZonedDateTime expiresAt
) {
}

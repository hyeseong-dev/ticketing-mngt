package com.mgnt.core.event;

import com.mgnt.core.enums.ReservationStatus;

public record ReservationResponseDTO(
        Long reservationId,
        Long userId,
        ReservationStatus status,
        ConcertInfoDTO concertInfo,
        PaymentInfoDTO paymentInfo
) {
}

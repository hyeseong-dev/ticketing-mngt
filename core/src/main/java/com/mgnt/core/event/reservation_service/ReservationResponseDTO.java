package com.mgnt.core.event.reservation_service;

import com.mgnt.core.enums.ReservationStatus;
import com.mgnt.core.event.payment_service.PaymentInfoDTO;
import com.mgnt.core.event.concert_service.ConcertInfoDTO;

public record ReservationResponseDTO(
        Long reservationId,
        Long userId,
        ReservationStatus status,
        ConcertInfoDTO concertInfo,
        PaymentInfoDTO paymentInfo
) {
}

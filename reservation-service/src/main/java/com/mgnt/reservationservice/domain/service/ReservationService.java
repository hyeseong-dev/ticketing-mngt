package com.mgnt.reservationservice.domain.service;

import com.mgnt.core.event.reservation_service.QueueEntryRequest;
import com.mgnt.core.event.reservation_service.QueueEntryResponse;
import com.mgnt.core.event.reservation_service.QueueStatusResponse;
import com.mgnt.core.event.reservation_service.ReservationInventoryCreateResponseDTO;
import com.mgnt.reservationservice.controller.dto.request.ReservationRequest;
import com.mgnt.reservationservice.controller.dto.request.ReserveRequest;
import com.mgnt.reservationservice.controller.dto.response.ReservationResponseDTO;
import com.mgnt.core.enums.ReservationStatus;
import com.mgnt.core.event.payment_service.PaymentCompletedEvent;
import com.mgnt.core.event.concert_service.SeatStatusUpdatedEvent;

import java.util.List;

public interface ReservationService {
    List<ReservationResponseDTO> getMyReservations(Long userId);

    void updateReservation(Long reservationId, ReservationStatus newStatus);

    void initiateReservation(Long userId, ReserveRequest request);

    void handleSeatStatusUpdate(SeatStatusUpdatedEvent event);

    void handlePaymentCompleted(PaymentCompletedEvent event);

    ReservationInventoryCreateResponseDTO createReservationWithoutPayment(Long userId, ReservationRequest request);
    
}
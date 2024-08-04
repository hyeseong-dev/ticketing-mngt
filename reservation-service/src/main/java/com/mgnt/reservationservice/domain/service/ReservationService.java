package com.mgnt.reservationservice.domain.service;

import com.mgnt.core.enums.ReservationStatus;
import com.mgnt.core.event.reservation_service.ReservationInventoryCreateResponseDTO;
import com.mgnt.reservationservice.controller.dto.request.ReservationInventoryRequest;
import com.mgnt.reservationservice.controller.dto.request.ReservationRequest;
import com.mgnt.reservationservice.controller.dto.request.TokenRequestDTO;
import com.mgnt.reservationservice.controller.dto.response.ReservationResponseDTO;
import com.mgnt.reservationservice.controller.dto.response.TokenResponseDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ReservationService {
    boolean initiateReservation(Long seatId, Long userId, Long concertId, Long concertDateId);

    List<ReservationResponseDTO> getMyReservations(Long userId);

    TokenResponseDTO getTokenStatus(Long userId, TokenRequestDTO request);

    void handleTempReservationExpiration(String expiredKey);

    CompletableFuture<ReservationInventoryCreateResponseDTO> createReservationWithoutPayment(Long userId, ReservationRequest request);
}
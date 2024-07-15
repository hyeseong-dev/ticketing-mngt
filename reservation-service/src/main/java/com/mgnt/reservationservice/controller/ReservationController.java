package com.mgnt.reservationservice.controller;

import com.mgnt.core.event.reservation_service.QueueEntryRequest;
import com.mgnt.core.event.reservation_service.QueueEntryResponse;
import com.mgnt.core.event.reservation_service.QueueStatusResponse;
import com.mgnt.core.event.reservation_service.ReservationInventoryCreateResponseDTO;
import com.mgnt.core.exception.ApiResult;
import com.mgnt.reservationservice.controller.dto.request.ReservationRequest;
import com.mgnt.reservationservice.controller.dto.request.ReserveRequest;
import com.mgnt.reservationservice.controller.dto.response.ReservationResponseDTO;
import com.mgnt.reservationservice.domain.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/api/reservations")
@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;

    @GetMapping("/me")
    public ApiResult<List<ReservationResponseDTO>> getMyReservations(@RequestHeader("User-Id") Long userId) {
        return ApiResult.success(service.getMyReservations(userId));

    }

    @PostMapping()
    public ApiResult<String> reserve(
            @RequestHeader("User-Id") Long userId,
            @RequestBody ReserveRequest request
    ) {

        service.initiateReservation(userId, request);
        return ApiResult.success("예약이 접수되었으며 진행중입니다.");
    }

    @PostMapping("/test")
    public ApiResult<ReservationInventoryCreateResponseDTO> reserveTest(
            @RequestHeader("User-Id") Long userId,
            @RequestBody ReservationRequest request
    ) {
        return ApiResult.success(service.createReservationWithoutPayment(userId, request));
    }

    @PostMapping("/queue")
    public ApiResult<QueueEntryResponse> reserveQueue(
            @RequestHeader("User-Id") Long userId,
            @RequestBody QueueEntryRequest request
    ) {
        return ApiResult.success(service.enterQueue(userId, request));
    }

    @GetMapping("/queue/status")
    public ApiResult<QueueStatusResponse> getQueueStatus(
            @RequestHeader("User-Id") Long userId,
            @RequestBody QueueEntryRequest request
    ) {
        
        return ApiResult.success(service.getQueueStatus(userId, request));
    }

}


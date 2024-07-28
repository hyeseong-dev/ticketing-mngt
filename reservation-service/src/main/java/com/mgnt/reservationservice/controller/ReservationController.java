package com.mgnt.reservationservice.controller;

import com.mgnt.core.event.reservation_service.QueueEntryRequest;
import com.mgnt.core.event.reservation_service.QueueEntryResponse;
import com.mgnt.core.event.reservation_service.QueueStatusResponse;
import com.mgnt.core.event.reservation_service.ReservationInventoryCreateResponseDTO;
import com.mgnt.core.exception.ApiResult;
import com.mgnt.reservationservice.controller.dto.request.ReservationRequest;
import com.mgnt.reservationservice.controller.dto.request.ReserveRequest;
import com.mgnt.reservationservice.controller.dto.request.TokenRequestDTO;
import com.mgnt.reservationservice.controller.dto.response.ReservationResponseDTO;
import com.mgnt.reservationservice.controller.dto.response.TokenResponseDTO;
import com.mgnt.reservationservice.domain.service.ReservationService;
import com.mgnt.reservationservice.kafka.ReservationProducer;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/api/reservations")
@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationProducer reservationProducer;

    @PostMapping("/select-seat/{seatId}")
    public ApiResult<String> selectSeat(
            @RequestHeader(value = "X-Concert-Id") Long concertId,
            @RequestHeader(value = "X-Concert-Date-Id") Long concertDateId,
            @RequestHeader(value = "X-User-Id") Long xUserId,
            @RequestHeader(value = "User-Id") Long userId,
            @RequestHeader("X-Reservation-Token") @NotNull String reservationToken,
            @PathParam(value = "seatId") @NotNull Long seatId
    ) {

        reservationProducer.initiateReservation(userId, reservationToken, xUserId, concertId, concertDateId, seatId);
        return ApiResult.success("좌석 선택이 접수되었으며 처리 중입니다.");
    }

    @PostMapping("/token")
    public ApiResult<TokenResponseDTO> getTokenStatus(
            @RequestHeader("User-Id") Long userId,
            @RequestBody TokenRequestDTO request
    ) {
        TokenResponseDTO response = reservationService.getTokenStatus(userId, request);
        return ApiResult.success(response);
    }

    @GetMapping("/me")
    public ApiResult<List<ReservationResponseDTO>> getMyReservations(@RequestHeader("User-Id") Long userId) {
        return ApiResult.success(reservationService.getMyReservations(userId));

    }

//    @PostMapping()
//    public ApiResult<String> reserve(
//            @RequestHeader("User-Id") Long userId,
//            @RequestBody ReserveRequest request
//    ) {
//        reservationService.initiateReservation(userId, request);
//        return ApiResult.success("예약이 접수되었으며 진행중입니다.");
//    }

    @PostMapping("/test")
    public ApiResult<ReservationInventoryCreateResponseDTO> reserveTest(
            @RequestHeader("User-Id") Long userId,
            @RequestBody ReservationRequest request
    ) {
        return ApiResult.success(reservationService.createReservationWithoutPayment(userId, request));
    }

}


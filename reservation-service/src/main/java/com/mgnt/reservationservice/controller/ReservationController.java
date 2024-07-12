package com.mgnt.reservationservice.controller;

import com.mgnt.core.exception.ApiResult;
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
    public ApiResult<ReservationResponseDTO> reserveTest(
            @RequestHeader("User-Id") Long userId,
            @RequestBody ReserveRequest request
    ) {
        return ApiResult.success(service.createReservationWithoutPayment(userId, request));
    }

//    @DeleteMapping("/{reservationId}")
//    public ApiResult<Void> cancel(@PathVariable(value = "reservationId") Long reservationId,
//                                  @RequestBody @Valid CancelRequest request) {
//        service.cancel(reservationId, request);
//        return ApiResult.successNoContent();
//
//    }

}


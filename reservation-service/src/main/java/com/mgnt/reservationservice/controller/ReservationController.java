package com.mgnt.reservationservice.controller;

//import com.mgnt.core.exception.ApiResult;
//import com.mgnt.reservationservice.controller.dto.request.CancelRequest;
//import com.mgnt.reservationservice.controller.dto.request.ReserveRequest;
//import com.mgnt.reservationservice.controller.dto.response.ReserveResponse;
//import com.mgnt.reservationservice.domain.service.ReservationService;
//import jakarta.validation.Valid;
//import jakarta.validation.constraints.NotNull;

import com.mgnt.core.exception.ApiResult;
import com.mgnt.reservationservice.controller.dto.request.CancelRequest;
import com.mgnt.reservationservice.controller.dto.request.ReserveRequest;
import com.mgnt.reservationservice.controller.dto.response.ReserveResponse;
import com.mgnt.reservationservice.domain.service.ReservationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/reservations")
@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;

//    @GetMapping("/me")
//    public ApiResult<List<ReserveResponse>> getMyReservation(
//            @RequestHeader("User-Id") @NotNull Long userId,
//            @RequestHeader("User-Role") @NotNull String userRole
//    ) {
//        return ApiResult.success(service.getMyReservations(userId, userRole));
//    }

    @PostMapping()
    public ApiResult<ReserveResponse> reserve(
            @RequestHeader("User-Id") @NotNull Long userId,
            @RequestHeader("User-Role") @NotNull String userRole,
            @RequestBody @Valid ReserveRequest request) {
        return ApiResult.success(service.reserve(request));
    }

//    @DeleteMapping("/{reservationId}")
//    public ApiResult<Void> cancel(@PathVariable(value = "reservationId") Long reservationId,
//                                  @RequestBody @Valid CancelRequest request) {
//        service.cancel(reservationId, request);
//        return ApiResult.successNoContent();
//
//    }

}


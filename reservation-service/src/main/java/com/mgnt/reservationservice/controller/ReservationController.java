package com.mgnt.reservationservice.controller;

//import com.mgnt.core.exception.ApiResult;
//import com.mgnt.reservationservice.controller.dto.request.CancelRequest;
//import com.mgnt.reservationservice.controller.dto.request.ReserveRequest;
//import com.mgnt.reservationservice.controller.dto.response.ReserveResponse;
//import com.mgnt.reservationservice.domain.service.ReservationService;
//import jakarta.validation.Valid;
//import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgnt.core.exception.ApiResult;
import com.mgnt.reservationservice.controller.dto.request.CancelRequest;
import com.mgnt.reservationservice.controller.dto.request.ReserveRequest;
import com.mgnt.reservationservice.controller.dto.response.ReserveResponse;
import com.mgnt.reservationservice.domain.service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@Slf4j
@RequestMapping("/api/reservations")
@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;
//
//    @GetMapping("/me")
//    public ApiResult<List<ReserveResponse>> getMyReservation(
//            @RequestHeader("User-Id") @NotNull Long userId,
//            @RequestHeader("User-Role") @NotNull String userRole
//    ) {
//        return ApiResult.success(service.getMyReservations(userId, userRole));
//    }


    @PostMapping()
    public ApiResult<String> reserve(
            @RequestHeader("User-Id") Long userId,
            @RequestBody ReserveRequest request
    ) {

        service.initiateReservation(userId, request);
        return ApiResult.success("예약이 접수되었으며 진행중입니다.");
    }

//    @DeleteMapping("/{reservationId}")
//    public ApiResult<Void> cancel(@PathVariable(value = "reservationId") Long reservationId,
//                                  @RequestBody @Valid CancelRequest request) {
//        service.cancel(reservationId, request);
//        return ApiResult.successNoContent();
//
//    }

}


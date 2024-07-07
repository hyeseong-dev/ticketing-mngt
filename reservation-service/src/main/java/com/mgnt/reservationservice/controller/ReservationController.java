package com.mgnt.reservationservice.controller;

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

@RequestMapping("/reservations")
@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;

    @PostMapping("")
    public ApiResult<ReserveResponse> reserve(@RequestBody @Valid ReserveRequest request) {
        return ApiResult.success(service.reserve(request));
    }

    @DeleteMapping("/{reservationId}")
    public ApiResult<Void> cancel(@PathVariable(value = "reservationId") Long reservationId,
                                  @RequestBody @Valid CancelRequest request) {
        service.cancel(reservationId, request);
        return ApiResult.successNoContent();

    }

    @GetMapping("/me")
    public ApiResult<List<ReserveResponse>> getMyReservation(@RequestParam(value = "userId") @NotNull Long userId) {
        return ApiResult.success(service.getMyReservations(userId));
    }
}
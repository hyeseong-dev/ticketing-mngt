package com.mgnt.ticketing.controller.reservation.dto;

import com.mgnt.ticketing.base.exception.ApiResult;
import com.mgnt.ticketing.controller.reservation.dto.request.CancelRequest;
import com.mgnt.ticketing.controller.reservation.dto.request.ReserveRequest;
import com.mgnt.ticketing.controller.reservation.dto.response.ReserveResponse;
import com.mgnt.ticketing.controller.user.dto.response.GetMyReservationsResponse;
import com.mgnt.ticketing.domain.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "예약", description = "reservation-controller")
@RequestMapping("/reservations")
@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;

    @Operation(summary = "예약")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ReserveResponse.class)))
    @PostMapping("")
    public ApiResult<ReserveResponse> reserve(@RequestBody @Valid ReserveRequest request) {
        return ApiResult.success(service.reserve(request));
    }

    @Operation(summary = "예약 취소")
    @ApiResponse(responseCode = "200", description = "OK")
    @DeleteMapping("/{reservationId}")
    public ApiResult<Void> cancel(@PathVariable(value = "reservationId") Long reservationId,
                                  @RequestBody @Valid CancelRequest request) {
        service.cancel(reservationId, request);
        return ApiResult.successNoContent();

    }

    @Operation(summary = "나의 예약 내역 조회")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GetMyReservationsResponse.class))))
    @GetMapping("/me")
    public ApiResult<List<GetMyReservationsResponse>> getMyReservation(@RequestParam(value = "userId") @NotNull Long userId) {
        return ApiResult.success(service.getMyReservations(userId));
    }
}
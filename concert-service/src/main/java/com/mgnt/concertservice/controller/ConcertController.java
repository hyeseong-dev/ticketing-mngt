package com.mgnt.concertservice.controller;

import com.mgnt.concertservice.controller.response.GetConcertResponse;
import com.mgnt.concertservice.controller.response.GetConcertsResponse;
import com.mgnt.concertservice.controller.response.GetDatesResponse;
import com.mgnt.concertservice.controller.response.GetSeatsResponse;
import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.concertservice.domain.service.ConcertService;
import com.mgnt.concertservice.domain.service.SeatService;
import com.mgnt.core.exception.ApiResult;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/concerts")
@RestController
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;
    private final SeatService seatService;

    @GetMapping("")
    public ApiResult<List<GetConcertsResponse>> getConcerts() {
        return ApiResult.success(concertService.getConcerts());
    }

    @GetMapping("/{concertId}")
    public ApiResult<GetConcertResponse> getConcert(@PathVariable(value = "concertId") @NotNull Long concertId) {
        return ApiResult.success(concertService.getConcert(concertId));
    }

    @GetMapping("/{concertId}/dates")
    public ApiResult<GetDatesResponse> getDates(@PathVariable(value = "concertId") @NotNull Long concertId) {
        return ApiResult.success(concertService.getDates(concertId));
    }

    @GetMapping("/dates/{concertDateId}/available-seats")
    public ApiResult<List<Seat>> getAvailableSeats(
            @RequestHeader(value = "X-Concert-Id") Long concertId,
            @RequestHeader(value = "X-Concert-Date-Id") Long xConcertDateId,
            @RequestHeader(value = "X-User-Id") Long xUserId,
            @RequestHeader(value = "User-Id") Long userId,
            @PathVariable(value = "concertDateId") @NotNull Long concertDateId
    ) {
        return ApiResult.success(seatService.getAvailableSeats(concertDateId));
    }

    @GetMapping("/dates/{concertDateId}/all-seats")
    public ApiResult<List<Seat>> getAllSeats(
            @RequestHeader(value = "User-Id") Long userId,
            @PathVariable(value = "concertDateId") @NotNull Long concertDateId
    ) {
        return ApiResult.success(seatService.getAllSeatsByConcertDateId(concertDateId));
    }
}

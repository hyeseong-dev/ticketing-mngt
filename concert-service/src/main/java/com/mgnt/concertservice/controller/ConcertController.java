package com.mgnt.concertservice.controller;

import com.mgnt.concertservice.controller.response.GetConcertResponse;
import com.mgnt.concertservice.controller.response.GetConcertsResponse;
import com.mgnt.concertservice.controller.response.GetDatesResponse;
import com.mgnt.concertservice.controller.response.GetSeatsResponse;
import com.mgnt.concertservice.domain.service.ConcertService;
import com.mgnt.core.exception.ApiResult;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/concerts")
@RestController
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService service;

    @GetMapping("")
    public ApiResult<List<GetConcertsResponse>> getConcerts() {
        return ApiResult.success(service.getConcerts());
    }

    @GetMapping("/{concertId}")
    public ApiResult<GetConcertResponse> getConcert(@PathVariable(value = "concertId") @NotNull Long concertId) {
        return ApiResult.success(service.getConcert(concertId));
    }

    @GetMapping("/{concertId}/dates")
    public ApiResult<GetDatesResponse> getDates(@PathVariable(value = "concertId") @NotNull Long concertId) {
        return ApiResult.success(service.getDates(concertId));
    }

    @GetMapping("/dates/{concertDateId}/seats")
    public ApiResult<GetSeatsResponse> getAvailableSeats(
            @RequestHeader(value = "X-Concert-Id") Long concertId,
            @RequestHeader(value = "X-Concert-Date-Id") Long xConcertDateId,
            @RequestHeader(value = "X-User-Id") Long xUserId,
            @RequestHeader(value = "User-Id") Long userId,
            @PathVariable(value = "concertDateId") @NotNull Long concertDateId
    ) {
        return ApiResult.success(service.getAvailableSeats(concertDateId));
    }
}

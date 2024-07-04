package com.mgnt.temp.controller;

import com.mgnt.temp.domain.service.ConcertService;
import com.mgnt.temp.controller.response.GetConcertResponse;
import com.mgnt.temp.controller.response.GetConcertsResponse;
import com.mgnt.temp.controller.response.GetDatesResponse;
import com.mgnt.temp.controller.response.GetSeatsResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService service;

    @Operation(summary = "콘서트 목록 조회")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GetConcertsResponse.class))))
    @GetMapping("")
    public ApiResult<List<GetConcertsResponse>> getConcerts() {
        return ApiResult.success(service.getConcerts());
    }

    @Operation(summary = "콘서트 상세 조회")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = GetConcertResponse.class)))
    @GetMapping("/{concertId}")
    public ApiResult<GetConcertResponse> getConcert(@PathVariable(value = "concertId") @NotNull Long concertId) {
        return ApiResult.success(service.getConcert(concertId));
    }

    @Operation(summary = "콘서트 회차 목록 조회")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GetDatesResponse.class))))
    @GetMapping("/{concertId}/dates")
    public ApiResult<GetDatesResponse> getDates(@PathVariable(value = "concertId") @NotNull Long concertId) {
        return ApiResult.success(service.getDates(concertId));
    }

    @Operation(summary = "예약 가능한 좌석 목록 조회")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GetSeatsResponse.class))))
    @GetMapping("/dates/{concertDateId}/seats")
    public ApiResult<GetSeatsResponse> getAvailableSeats(@PathVariable(value = "concertDateId") @NotNull Long concertDateId) {
        return ApiResult.success(service.getAvailableSeats(concertDateId));
    }
}

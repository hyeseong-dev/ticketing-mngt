package com.mgnt.ticketing.controller.concert.dto;


import com.mgnt.ticketing.controller.concert.dto.response.GetConcertResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetConcertsResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetDatesResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetSeatsResponse;
import com.mgnt.ticketing.domain.concert.service.ConcertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.List;

@Tag(name = "콘서트", description = "concert-controller")
@RequestMapping("/concerts")
@RestController
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService service;

    @Operation(summary = "콘서트 목록 조회")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GetConcertsResponse.class))))
    @GetMapping("")
    public List<GetConcertsResponse> getConcerts() {
        // dummy data
        return List.of(
                new GetConcertsResponse(1L, "2024 테스트 콘서트 in 서울", ZonedDateTime.now().minusDays(1)),
                new GetConcertsResponse(2L, "2024 더미 콘서트 in 부산", ZonedDateTime.now().minusDays(3))
        );
    }

    @Operation(summary = "콘서트 상세 조회")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = GetConcertResponse.class)))
    @GetMapping("/{concertId}")
    public GetConcertResponse getConcert(@PathVariable(value = "concertId") @NotNull Long concertId) {
        // dummy data
        return GetConcertResponse.builder()
                .concertId(1L)
                .name("2024 테스트 콘서트 in 서울")
                .place("서울 장충체육관")
                .period("2024.05.05~2024.05.25")
                .price("79,000원~119,000원")
                .createdAt(ZonedDateTime.now().minusDays(1))
                .build();
    }
    @Operation(summary = "콘서트 회차 목록 조회")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GetDatesResponse.class))))
    @GetMapping("/{concertId}/dates")
    public List<GetDatesResponse> getDates(@PathVariable(value = "concertId") @NotNull Long concertId) {
        // dummy data
        return List.of(
                new GetDatesResponse(1L, ZonedDateTime.now().plusMonths(1)),
                new GetDatesResponse(2L, ZonedDateTime.now().plusMonths(2))
        );
    }

    @Operation(summary = "좌석 목록 조회")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = GetSeatsResponse.class))))
    @GetMapping("/{concertId}/dates/{concertDateId}/seats")
    public List<GetSeatsResponse> getSeats(@PathVariable(value = "concertId") @NotNull Long concertId,
                                           @PathVariable(value = "concertDateId") @NotNull Long concertDateId) {
        // dummy data
        return List.of(
                new GetSeatsResponse(1L, 1, false),
                new GetSeatsResponse(2L, 2, false),
                new GetSeatsResponse(3L, 3, true),
                new GetSeatsResponse(4L, 4, true),
                new GetSeatsResponse(5L, 5, false)
        );
    }
}

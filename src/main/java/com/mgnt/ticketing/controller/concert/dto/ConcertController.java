package com.mgnt.ticketing.controller.concert.dto;


import com.mgnt.ticketing.controller.concert.dto.response.GetConcertResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetConcertsResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetDatesResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetSeatsResponse;
import com.mgnt.ticketing.domain.concert.service.ConcertService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.List;

@RequestMapping("/concerts")
@RestController
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService service;

    @GetMapping("/")
    public List<GetConcertsResponse> getConcerts() {
        // dummy data
        return List.of(
                new GetConcertsResponse(1L, "2024 테스트 콘서트 in 서울", ZonedDateTime.now().minusDays(1)),
                new GetConcertsResponse(2L, "2024 더미 콘서트 in 부산", ZonedDateTime.now().minusDays(3))
        );
    }

    @GetMapping("/{concertId}")
    public GetConcertResponse getConcert(@PathVariable(value = "concertId") @NotNull Long concertId) {
        // dummy data
        return GetConcertResponse.builder()
                .concertId(1L)
                .name("2024 테스트 콘서트 in 서울")
                .hall("서울 장충체육관")
                .period("2024.05.05~2024.05.25")
                .price("79,000원~119,000원")
                .createdAt(ZonedDateTime.now().minusDays(1))
                .build();
    }

    @GetMapping("/{concertId}/dates")
    public List<GetDatesResponse> getDates(@PathVariable(value = "concertId") @NotNull Long concertId) {
        // dummy data
        return List.of(
                new GetDatesResponse(1L, ZonedDateTime.now().plusMonths(1)),
                new GetDatesResponse(2L, ZonedDateTime.now().plusMonths(2))
        );
    }

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

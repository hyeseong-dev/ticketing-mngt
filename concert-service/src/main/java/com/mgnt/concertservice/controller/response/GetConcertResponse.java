package com.mgnt.concertservice.controller.response;

import com.mgnt.concertservice.domain.entity.Concert;
import com.mgnt.concertservice.domain.entity.ConcertDate;
import lombok.Builder;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public record GetConcertResponse(
        Long concertId,
        String name,
        String place,
        String period,
        String price,
        ZonedDateTime createdAt
) {
    @Builder
    public GetConcertResponse {
    }

    public static GetConcertResponse from(Concert concert) {
        return GetConcertResponse.builder()
                .concertId(concert.getConcertId())
                .name(concert.getName())
                .place(concert.getPlace() != null ? concert.getPlace().getName() : "-")
                .period(getConcertDateRange(concert.getConcertDateList()))
                .createdAt(concert.getCreatedAt())
                .build();
    }

    private static String getConcertDateRange(List<ConcertDate> concertDateList) {
        // 콘서트 날짜 범위 문자열로 반환
        if (concertDateList.isEmpty()) {
            return "";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        List<ConcertDate> sortedConcertDateList = concertDateList.stream()
                .sorted(Comparator.comparing(ConcertDate::getConcertDate))
                .toList();
        String earliestDate = formatter.format(sortedConcertDateList.get(0).getConcertDate());
        String latestDate = formatter.format(sortedConcertDateList.get(sortedConcertDateList.size() - 1).getConcertDate());

        return earliestDate + "~" + latestDate;
    }

}

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
        String concertName,
        String placeName,
        String period,
        String price,
        ZonedDateTime createdAt
) {
    @Builder
    public GetConcertResponse {
    }

    public static GetConcertResponse of(Concert concert, String placeName, String price) {
        return GetConcertResponse.builder()
                .concertId(concert.getConcertId())
                .concertName(concert.getName())
                .placeName(placeName)
                .period(getConcertDateRange(concert.getConcertDateList()))
                .price(price)
                .createdAt(concert.getCreatedAt())
                .build();
    }

    private static String getConcertDateRange(List<ConcertDate> concertDateList) {
        // 기존 로직 유지
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
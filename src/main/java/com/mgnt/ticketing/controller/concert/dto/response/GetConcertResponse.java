package com.mgnt.ticketing.controller.concert.dto.response;

import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import com.mgnt.ticketing.domain.concert.entity.Place;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import lombok.Builder;

import java.text.DecimalFormat;
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

    public static GetConcertResponse from(Concert concert, Place place) {
        return GetConcertResponse.builder()
                .concertId(concert.getConcertId())
                .name(concert.getName())
                .place(place != null ? place.getName() : "-")
                .period(getConcertDateRange(concert.getConcertDateList()))
                .price(place != null ? getSeatPriceRange(place.getSeatList()) : "-")
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

    private static String getSeatPriceRange(List<Seat> seatList) {
        // 좌석 가격 범위 문자열로 반환
        if (seatList.isEmpty()) {
            return "";
        }

        DecimalFormat formatter = new DecimalFormat("###,###원");

        List<Seat> sortedSeatList = seatList.stream()
                .sorted(Comparator.comparing(Seat::getPrice))
                .toList();
        String lowestPrice = formatter.format(sortedSeatList.get(0).getPrice());
        String largestPrice = formatter.format(sortedSeatList.get(sortedSeatList.size() - 1).getPrice());

        return lowestPrice + "~" + largestPrice;
    }
}

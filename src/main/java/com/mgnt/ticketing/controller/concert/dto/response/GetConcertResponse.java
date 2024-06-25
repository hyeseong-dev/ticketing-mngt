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

// 콘서트 정보 응답을 위한 레코드 클래스
public record GetConcertResponse(
        Long concertId,
        String name,
        String hall,
        String period,
        String price,
        ZonedDateTime createdAt
) {
    // Builder 패턴을 사용하기 위한 생성자
    @Builder
    public GetConcertResponse {
    }

    // Concert와 Place 객체로부터 GetConcertResponse 객체를 생성하는 정적 메서드
    public static GetConcertResponse from(Concert concert, Place place) {
        return GetConcertResponse.builder()
                .concertId(concert.getConcertId())       // 콘서트 ID 설정
                .name(concert.getName())                 // 콘서트 이름 설정
                .hall(place != null ? place.getName() : null)  // 장소 이름 설정 (null 처리 포함)
                .period(getConcertDateRange(concert.getConcertDateList())) // 콘서트 기간 설정
                .price(place != null ? getSeatPriceRange(place.getSeatList()) : null) // 좌석 가격 범위 설정 (null 처리 포함)
                .createdAt(concert.getCreatedAt())       // 콘서트 생성 날짜 설정
                .build();
    }

    // 콘서트 날짜 목록으로부터 날짜 범위 문자열을 생성하는 메서드
    private static String getConcertDateRange(List<ConcertDate> concertDateList) {
        // 날짜 목록이 비어있는 경우 빈 문자열 반환
        if (concertDateList.isEmpty()) {
            return "";
        }

        // 날짜 형식을 지정하는 포맷터
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        // 콘서트 날짜 목록을 날짜 기준으로 정렬
        concertDateList.sort(Comparator.comparing(ConcertDate::getConcertDate));
        String earliestDate = formatter.format(concertDateList.get(0).getConcertDate());  // 가장 빠른 날짜
        String latestDate = formatter.format(concertDateList.get(concertDateList.size() - 1).getConcertDate());  // 가장 늦은 날짜

        // "시작일~종료일" 형식의 문자열 반환
        return earliestDate + "~" + latestDate;
    }

    // 좌석 목록으로부터 가격 범위 문자열을 생성하는 메서드
    private static String getSeatPriceRange(List<Seat> seatList) {
        // 좌석 목록이 비어있는 경우 빈 문자열 반환
        if (seatList.isEmpty()) {
            return "";
        }

        // 가격 형식을 지정하는 포맷터
        DecimalFormat formatter = new DecimalFormat("###,###원");

        // 좌석 목록을 가격 기준으로 정렬
        seatList.sort(Comparator.comparing(Seat::getPrice));
        String lowestPrice = formatter.format(seatList.get(0).getPrice());  // 가장 낮은 가격
        String largestPrice = formatter.format(seatList.get(seatList.size() - 1).getPrice());  // 가장 높은 가격

        // "최저가격~최고가격" 형식의 문자열 반환
        return lowestPrice + "~" + largestPrice;
    }
}
package com.mgnt.ticketing.domain.concert.service;

import com.mgnt.ticketing.controller.concert.dto.response.GetConcertResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetConcertsResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetDatesResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetSeatsResponse;

import java.util.List;

public interface ConcertInterface {

    /* 콘서트 목록 조회 */
    List<GetConcertsResponse> getConcerts();

    /* 콘서트 상세 조회 */
    GetConcertResponse getConcert(Long concertId);

    /* 예약 가능 날짜 조회 */
    GetDatesResponse getDates(Long concertId);

    /* 좌석 조회 */
    GetSeatsResponse getSeats(Long concertId, Long concertDateId);
}

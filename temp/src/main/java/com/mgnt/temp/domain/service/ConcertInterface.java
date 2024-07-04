package com.mgnt.temp.domain.service;

import com.mgnt.temp.controller.response.GetConcertResponse;
import com.mgnt.temp.controller.response.GetConcertsResponse;
import com.mgnt.temp.controller.response.GetDatesResponse;
import com.mgnt.temp.controller.response.GetSeatsResponse;
import com.mgnt.temp.domain.entity.Seat;

import java.util.List;

public interface ConcertInterface {

    /* 콘서트 목록 조회 */
    List<GetConcertsResponse> getConcerts();

    /* 콘서트 상세 조회 */
    GetConcertResponse getConcert(Long concertId);

    /* 날짜 목록 조회 */
    GetDatesResponse getDates(Long concertId);

    /* 예약 가능 좌석 조회 */
    GetSeatsResponse getAvailableSeats(Long concertDateId);

    /* 좌석 상태 변경 */
    void patchSeatStatus(Long concertDateId, int seatNum, Seat.Status status);
}


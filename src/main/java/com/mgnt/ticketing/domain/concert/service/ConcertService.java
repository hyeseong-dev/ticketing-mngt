package com.mgnt.ticketing.domain.concert.service;

import com.mgnt.ticketing.controller.concert.dto.response.GetConcertResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetConcertsResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetDatesResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetSeatsResponse;
import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.Place;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.concert.repository.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 콘서트 서비스 클래스
 *
 * 이 클래스는 콘서트와 관련된 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
public class ConcertService implements ConcertInterface {

    private final ConcertRepository concertRepository;
    private final ConcertValidator concertValidator;
    private final ConcertReader concertReader;
    private final ConcertPlaceManager placeManager;
    private final ConcertReservationManager reservationManager;

    /**
     * 모든 콘서트 목록 조회
     *
     * @return 콘서트 목록 응답 DTO 리스트
     */
    @Override
    public List<GetConcertsResponse> getConcerts() {
        List<Concert> concerts = concertRepository.findAll();
        return concerts.stream().map(GetConcertsResponse::from).toList();
    }

    /**
     * 특정 콘서트 조회
     *
     * @param concertId 콘서트 ID
     * @return 콘서트 응답 DTO
     */
    @Override
    public GetConcertResponse getConcert(Long concertId) {
        Concert concert = concertRepository.findById(concertId);
        Place place = concertReader.findPlace(concert.getPlaceId());
        return GetConcertResponse.from(concert, place);
    }

    /**
     * 특정 콘서트의 날짜 목록 조회
     *
     * @param concertId 콘서트 ID
     * @return 콘서트 날짜 응답 DTO 리스트
     */
    @Override
    public GetDatesResponse getDates(Long concertId) {
        Concert concert = concertRepository.findById(concertId);
        // validator
        concertValidator.dateIsNull(concert.getConcertDateList());

        return GetDatesResponse.from(concert.getConcertDateList());
    }

    @Override
    public GetSeatsResponse getSeats(Long concertId, Long concertDateId) {
        // 콘서트 전체 좌석 정보
        List<Seat> allSeats = placeManager.getSeatsByConcertId(concertId);
        // 예약된 좌석 PK 조회
        List<Long> reservedSeatIds = reservationManager.getReservedSeatIdsByConcertDate(concertDateId);

        return GetSeatsResponse.from(allSeats, reservedSeatIds);
    }

}

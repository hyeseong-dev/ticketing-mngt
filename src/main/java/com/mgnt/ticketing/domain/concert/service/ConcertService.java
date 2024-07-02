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

import java.util.ArrayList;
import java.util.List;

/**
 * 콘서트 서비스 클래스
 * <p>
 * 이 클래스는 콘서트와 관련된 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
public class ConcertService implements ConcertInterface {

    private final ConcertRepository concertRepository;
    private final ConcertValidator concertValidator;

    @Override
    public List<GetConcertsResponse> getConcerts() {
        List<Concert> concerts = concertRepository.findAll();
        return concerts.stream().map(GetConcertsResponse::from).toList();
    }

    @Override
    public GetConcertResponse getConcert(Long concertId) {
        Concert concert = concertRepository.findById(concertId);
        return GetConcertResponse.from(concert);
    }

    @Override
    public GetDatesResponse getDates(Long concertId) {
        Concert concert = concertRepository.findById(concertId);
        // validator
        concertValidator.dateIsNull(concert.getConcertDateList());

        // 콘서트 날짜 예약 가능 여부
        List<GetDatesResponse.DateInfo> dateInfos = new ArrayList<>();
        concert.getConcertDateList().forEach(concertDate -> {
            boolean available = concertRepository.existByConcertDateAndStatus(concertDate.getConcertDateId(), Seat.Status.AVAILABLE);
            dateInfos.add(GetDatesResponse.DateInfo.from(concertDate, available));
        });

        return new GetDatesResponse(dateInfos);
    }

    @Override
    public GetSeatsResponse getAvailableSeats(Long concertDateId) {
        List<Seat> availableSeats = concertRepository.findSeatsByConcertDateIdAndStatus(concertDateId, Seat.Status.AVAILABLE);
        return GetSeatsResponse.from(availableSeats);
    }

    @Override
    public void patchSeatStatus(Long concertDateId, int seatNum, Seat.Status status) {
        Seat seat = concertRepository.findSeatByConcertDateIdAndSeatNum(concertDateId, seatNum);
        if (seat != null) {
            seat.patchStatus(status);
        }
    }
}

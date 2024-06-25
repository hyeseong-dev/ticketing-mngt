package com.mgnt.ticketing.domain.concert.service;

import com.mgnt.ticketing.controller.concert.dto.response.GetConcertResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetConcertsResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetDatesResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetSeatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertService implements ConcertInterface {

    @Override
    public List<GetConcertsResponse> getConcerts() {
        return null;
    }

    @Override
    public GetConcertResponse getConcert(Long concertId) {
        return null;
    }

    @Override
    public List<GetDatesResponse> getDates(Long concertId) {
        return null;
    }

    @Override
    public List<GetSeatsResponse> getSeats(Long concertId, Long concertDateId) {
        return null;
    }
}

package com.mgnt.ticketing.domain.concert.service;

import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import com.mgnt.ticketing.domain.concert.entity.Place;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.concert.repository.ConcertRepository;

import com.mgnt.ticketing.domain.concert.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 콘서트 정보 조회 서비스
 * <p>
 * 이 클래스는 콘서트와 관련된 정보를 단순 조회하는 기능을 제공합니다.
 */
@Component
@RequiredArgsConstructor
public class ConcertReader {
    /* Concert 관련 정보 단순 조회용 */

    private final ConcertRepository concertRepository;

    public Concert findConcert(Long concertId) {
        return concertRepository.findById(concertId);
    }

    public ConcertDate findConcertDate(Long concertDateId) {
        return concertRepository.findConcertDateById(concertDateId);
    }
    
    public Seat findSeat(Long concertDateId, int seatNum) {
        return concertRepository.findSeatByConcertDateIdAndSeatNum(concertDateId, seatNum);
    }
}
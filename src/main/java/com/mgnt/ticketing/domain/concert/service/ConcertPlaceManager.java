package com.mgnt.ticketing.domain.concert.service;

import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.Place;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.concert.repository.ConcertRepository;
import com.mgnt.ticketing.domain.concert.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 콘서트 장소 관리 서비스
 *
 * 이 클래스는 콘서트와 공연장 정보를 관리하는 기능을 제공합니다.
 */
@Component
@RequiredArgsConstructor
public class ConcertPlaceManager {

    private final ConcertRepository concertRepository;
    private final PlaceRepository placeRepository;

    /**
     * 콘서트 ID로 공연장 전체 좌석 조회
     *
     * @param concertId 콘서트 ID
     * @return 좌석 목록
     */
    public List<Seat> getSeatsByConcertId(Long concertId) {
        Concert concert = concertRepository.findById(concertId);
        return placeRepository.findById(concert.getPlaceId()).getSeatList();
    }
}

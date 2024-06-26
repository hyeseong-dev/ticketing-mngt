package com.mgnt.ticketing.domain.place.service;

import com.mgnt.ticketing.domain.place.entity.Place;
import com.mgnt.ticketing.domain.place.entity.Seat;

import java.util.List;

public interface PlaceInterface {

    // 공연장 좌석 조회
    List<Seat> getSeatsByPlace(Long placeId);

    // 공연장 조회
    Place getPlace(Long placeId);
}

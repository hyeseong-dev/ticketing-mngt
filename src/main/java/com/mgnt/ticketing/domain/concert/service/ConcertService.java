package com.mgnt.ticketing.domain.concert.service;

import com.mgnt.ticketing.controller.concert.dto.response.GetConcertResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetConcertsResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetDatesResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetSeatsResponse;
import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.repository.ConcertRepository;
import com.mgnt.ticketing.domain.place.entity.Place;
import com.mgnt.ticketing.domain.place.entity.Seat;
import com.mgnt.ticketing.domain.place.service.PlaceService;
import com.mgnt.ticketing.domain.reservation.ReservationEnums;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConcertService implements ConcertInterface {

    private final ConcertRepository concertRepository;
    private final ConcertValidator concertValidator;

    private final PlaceService placeService;
    private final ReservationService reservationService;

    @Override
    public List<GetConcertsResponse> getConcerts() {
        List<Concert> concerts = concertRepository.findAll();
        return concerts.stream().map(GetConcertsResponse::from).toList();
    }

    @Override
    public GetConcertResponse getConcert(Long concertId) {
        Concert concert = concertRepository.findById(concertId);
        Place place = placeService.getPlace(concert.getPlaceId());
        return GetConcertResponse.from(concert, place);
    }

    @Override
    public List<GetDatesResponse> getDates(Long concertId) {
        Concert concert = concertRepository.findById(concertId);
        // validator
        concertValidator.dateIsNull(concert.getConcertDateList());

        return concert.getConcertDateList().stream().map(GetDatesResponse::from).toList();
    }

    @Override
    public List<GetSeatsResponse> getSeats(Long concertId, Long concertDateId) {
        // 콘서트 전체 좌석 정보
        Concert concert = concertRepository.findById(concertId);
        List<Seat> allSeats = placeService.getSeatsByPlace(concert.getPlaceId());

        // 예약된 좌석 조회
        List<Reservation> reservations = reservationService.getReservationsByConcertDate(concertDateId);
        List<Long> reservedSeatIds = reservations.stream()
                .filter(v -> List.of(ReservationEnums.Status.RESERVED, ReservationEnums.Status.ING).contains(v.getStatus()))
                .map(Reservation::getSeat)
                .map(Seat::getSeatId)
                .toList();

        return allSeats.stream()
                .map(seat -> new GetSeatsResponse(seat.getSeatId(), seat.getSeatNum(), !reservedSeatIds.contains(seat.getSeatId())))
                .collect(Collectors.toList());
    }
}
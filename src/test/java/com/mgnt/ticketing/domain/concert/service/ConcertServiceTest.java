package com.mgnt.ticketing.domain.concert.service;

import static org.junit.jupiter.api.Assertions.*;

import com.mgnt.ticketing.controller.concert.dto.response.GetConcertResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetConcertsResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetDatesResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetSeatsResponse;
import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import com.mgnt.ticketing.domain.concert.repository.ConcertRepository;
import com.mgnt.ticketing.domain.place.entity.Place;
import com.mgnt.ticketing.domain.place.entity.Seat;
import com.mgnt.ticketing.domain.place.service.PlaceService;
import com.mgnt.ticketing.domain.reservation.ReservationEnums;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.reservation.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConcertServiceTest {

    private ConcertService concertService;
    private ConcertRepository concertRepository;
    private ConcertValidator concertValidator;
    private PlaceService placeService;
    private ReservationService reservationService;

    private Concert 임영웅_콘서트;
    private Place 서울_장충체육관;
    private List<Seat> 좌석_목록;
    private List<Reservation> 예약_목록;

    @BeforeEach
    void setUp() {
        // Mocking 설정
        concertRepository = Mockito.mock(ConcertRepository.class);
        concertValidator = Mockito.mock(ConcertValidator.class);
        placeService = Mockito.mock(PlaceService.class);
        reservationService = Mockito.mock(ReservationService.class);

        concertService = new ConcertService(
                concertRepository,
                concertValidator,
                placeService,
                reservationService
        );

        // 임영웅 콘서트 정보 세팅
        임영웅_콘서트 = Concert.builder()
                .concertId(1L)
                .name("2024 임영웅 콘서트 [IM HERO - THE STADIUM]")
                .placeId(1L)
                .concertDateList(new ArrayList<>()) // 빈 리스트로 초기화
                .build();

        // 장소 정보 세팅
        서울_장충체육관 = new Place("서울 장충체육관", 5000);

        // 좌석 목록 세팅
        좌석_목록 = List.of(
                new Seat(1, 50000),
                new Seat(2, 60000)
        );

        // 예약 목록 세팅
        예약_목록 = List.of(
                new Reservation(null, null, 좌석_목록.get(0), ReservationEnums.Status.RESERVED, ZonedDateTime.now())
        );
    }

    @Test
    @DisplayName("콘서트_전체_목록_조회")
    void getConcertsTest_콘서트_전체_목록_조회() {
        // given
        when(concertRepository.findAll()).thenReturn(List.of(임영웅_콘서트));

        // when
        List<GetConcertsResponse> concerts = concertService.getConcerts();

        // then
        assertNotNull(concerts);
        assertEquals(1, concerts.size());
        assertEquals("2024 임영웅 콘서트 [IM HERO - THE STADIUM]", concerts.get(0).name());
    }

    @Test
    @DisplayName("콘서트_상세_조회")
    void getConcertTest_콘서트_상세_조회() {
        // given
        when(concertRepository.findById(1L)).thenReturn(임영웅_콘서트);
        when(placeService.getPlace(1L)).thenReturn(서울_장충체육관);

        // when
        GetConcertResponse concertResponse = concertService.getConcert(1L);

        // then
        assertNotNull(concertResponse);
        assertEquals("2024 임영웅 콘서트 [IM HERO - THE STADIUM]", concertResponse.name());
        assertEquals("서울 장충체육관", concertResponse.hall());
    }

    @Test
    @DisplayName("콘서트_예약_가능_날짜_조회")
    void getDatesTest_콘서트_예약_가능_날짜_조회() {
        // given
        List<ConcertDate> concertDateList = List.of(
                new ConcertDate(ZonedDateTime.now().plusDays(1)),
                new ConcertDate(ZonedDateTime.now().plusDays(2))
        );
        임영웅_콘서트.setConcertDateList(concertDateList);

        when(concertRepository.findById(1L)).thenReturn(임영웅_콘서트);

        // when
        List<GetDatesResponse> dates = concertService.getDates(1L);

        // then
        assertNotNull(dates);
        assertEquals(2, dates.size());
    }

    @Test
    @DisplayName("콘서트_좌석_정보_조회")
    void getSeatsTest_콘서트_좌석_정보_조회() {
        // given
        Long concertId = 1L;
        Long concertDateId = 1L;

        when(concertRepository.findById(concertId)).thenReturn(임영웅_콘서트);
        when(placeService.getSeatsByPlace(any())).thenReturn(좌석_목록);
        when(reservationService.getReservationsByConcertDate(any())).thenReturn(예약_목록);

        // when
        List<GetSeatsResponse> seatResponses = concertService.getSeats(concertId, concertDateId);

        // then
        assertFalse(seatResponses.stream().anyMatch(seat -> seat.seatNum() == 1 && seat.isReserved()));
        assertTrue(seatResponses.stream().anyMatch(seat -> seat.seatNum() == 2 && !seat.isReserved()));
    }
}

package com.mgnt.ticketing.domain.unit;

import com.mgnt.ticketing.base.exception.CustomException;
import com.mgnt.ticketing.controller.concert.dto.response.GetConcertResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetConcertsResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetDatesResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetSeatsResponse;
import com.mgnt.ticketing.domain.concert.ConcertExceptionEnum;
import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import com.mgnt.ticketing.domain.concert.entity.Place;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.concert.repository.ConcertRepository;

import com.mgnt.ticketing.domain.concert.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.logging.LogLevel;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.doThrow;


class ConcertServiceTest {

    private ConcertService concertService;
    private ConcertRepository concertRepository;
    private ConcertValidator concertValidator;
    private ConcertReader concertReader;
    private ConcertPlaceManager placeManager;
    private ConcertReservationManager reservationManager;

    private Concert 임영웅_콘서트;
    private Place 상암_월드컵경기장;

    @BeforeEach
    void setUp() {
        // mocking
        concertRepository = Mockito.mock(ConcertRepository.class);
        concertValidator = Mockito.mock(ConcertValidator.class);
        concertReader = Mockito.mock(ConcertReader.class);
        placeManager = Mockito.mock(ConcertPlaceManager.class);
        reservationManager = Mockito.mock(ConcertReservationManager.class);

        concertService = new ConcertService(
                concertRepository,
                concertValidator,
                concertReader,
                placeManager,
                reservationManager
        );

        // 콘서트 정보 세팅
        임영웅_콘서트 = Concert.builder()
                .name("2024 임영웅 콘서트 [IM HERO - THE STADIUM]")
                .placeId(1L)
                .concertDateList(List.of(
                        new ConcertDate(1L,
                                ZonedDateTime.of(
                                        LocalDateTime.of(2024, 5, 25, 18, 30, 0),
                                        ZoneId.of("Asia/Seoul"))
                        ),
                        new ConcertDate(1L,
                                ZonedDateTime.of(
                                        LocalDateTime.of(2024, 5, 26, 19, 0, 0),
                                        ZoneId.of("Asia/Seoul")))
                ))
                .build();

        // 공연장 & 좌석 정보 세팅
        상암_월드컵경기장 = Place.builder()
                .name("상암 월드컵경기장")
                .seats_cnt(5)
                .seatList(List.of(
                        new Seat(1L, 1, BigDecimal.valueOf(119000)),
                        new Seat(2L, 2, BigDecimal.valueOf(119000)),
                        new Seat(3L, 3, BigDecimal.valueOf(139000)),
                        new Seat(4L, 4, BigDecimal.valueOf(139000)),
                        new Seat(5L, 5, BigDecimal.valueOf(179000))
                ))
                .build();
    }

    @Test
    @DisplayName("콘서트_전체_목록_조회")
    void getConcertsTest_콘서트_전체_목록_조회() {
        // when
        when(concertRepository.findAll()).thenReturn(List.of(임영웅_콘서트));
        List<GetConcertsResponse> responses = concertService.getConcerts();

        // then
        assertThat(responses.get(0).name()).isEqualTo("2024 임영웅 콘서트 [IM HERO - THE STADIUM]");
    }

    @Test
    @DisplayName("콘서트_상세정보_조회")
    void getConcertTest_콘서트_상세정보_조회() {
        // given
        Long concertId = 1L;

        // when
        when(concertRepository.findById(concertId)).thenReturn(임영웅_콘서트);
        when(concertReader.findPlace(임영웅_콘서트.getPlaceId())).thenReturn(상암_월드컵경기장);
        GetConcertResponse response = concertService.getConcert(concertId);

        // then
        assertThat(response.name()).isEqualTo("2024 임영웅 콘서트 [IM HERO - THE STADIUM]");
        assertThat(response.period()).isEqualTo("2024.05.25~2024.05.26");
        assertThat(response.place()).isEqualTo("상암 월드컵경기장");
        assertThat(response.price()).isEqualTo("119,000원~179,000원");
    }

    @Test
    @DisplayName("콘서트_공연장_정보_없으면_하이픈_반환")
    void getConcertTest_콘서트_공연장_정보_없으면_하이픈_반환() {
        // given
        Long concertId = 1L;

        // when
        when(concertRepository.findById(concertId)).thenReturn(임영웅_콘서트);
        when(concertReader.findPlace(임영웅_콘서트.getPlaceId())).thenReturn(null);
        GetConcertResponse response = concertService.getConcert(concertId);

        // then
        assertThat(response.name()).isEqualTo("2024 임영웅 콘서트 [IM HERO - THE STADIUM]");
        assertThat(response.period()).isEqualTo("2024.05.25~2024.05.26");
        assertThat(response.place()).isEqualTo("-");
        assertThat(response.price()).isEqualTo("-");
    }

    @Test
    @DisplayName("예정된_날짜가_없음")
    void getDatesTest_예정된_날짜가_없음() {
        // given
        Long concertId = 1L;

        // when
        when(concertRepository.findById(concertId)).thenReturn(Concert.builder()
                .name("날짜없는 콘서트")
                .placeId(1L)
                .concertDateList(new ArrayList<>())
                .build());
        doThrow(new CustomException(ConcertExceptionEnum.DATE_IS_NULL, null, LogLevel.INFO)).when(concertValidator).dateIsNull(any());

        // then
        CustomException expected = assertThrows(CustomException.class, () ->
                concertValidator.dateIsNull(new ArrayList<>()));
        assertThat(expected.getMessage()).isEqualTo("예정된 콘서트 날짜가 없습니다.");
    }

    @Test
    @DisplayName("콘서트_회차_목록_조회")
    void getDatesTest_콘서트_회차_목록_조회() {
        // given
        Long concertId = 1L;

        // when
        when(concertRepository.findById(concertId)).thenReturn(임영웅_콘서트);
        List<GetDatesResponse> responses = concertService.getDates(concertId);

        // then
        assertThat(responses.size()).isEqualTo(2);
        assertThat(responses.get(0).date()).isEqualTo(ZonedDateTime.of(
                LocalDateTime.of(2024, 5, 25, 18, 30, 0),
                ZoneId.of("Asia/Seoul")));
    }

    @Test
    @DisplayName("콘서트_회차별_좌석_목록_조회")
    void getSeatsTest_콘서트_회차별_좌석_목록_조회() {
        // given
        Long concertId = 1L;
        Long concertDateId = 1L;

        // when
        when(placeManager.getSeatsByConcertId(concertId)).thenReturn(상암_월드컵경기장.getSeatList());
        when(reservationManager.getReservedSeatIdsByConcertDate(concertDateId)).thenReturn(List.of(2L, 4L));
        List<GetSeatsResponse> responses = concertService.getSeats(concertId, concertDateId);

        // then
        // 전체 좌석 중 2,4번 좌석만 예약 여부 true
        assertThat(responses.size()).isEqualTo(5);
        assertThat(responses.get(0).isReserved()).isEqualTo(false);
        assertThat(responses.get(1).isReserved()).isEqualTo(true);
        assertThat(responses.get(2).isReserved()).isEqualTo(false);
        assertThat(responses.get(3).isReserved()).isEqualTo(true);
        assertThat(responses.get(4).isReserved()).isEqualTo(false);
    }
}
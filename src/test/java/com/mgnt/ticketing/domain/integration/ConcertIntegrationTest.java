package com.mgnt.ticketing.domain.integration;

import com.mgnt.ticketing.base.exception.CustomException;
import com.mgnt.ticketing.controller.concert.dto.response.GetConcertResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetDatesResponse;
import com.mgnt.ticketing.controller.concert.dto.response.GetSeatsResponse;
import com.mgnt.ticketing.domain.concert.ConcertExceptionEnum;
import com.mgnt.ticketing.domain.concert.repository.ConcertRepository;
import com.mgnt.ticketing.domain.integration.base.BaseIntegrationTest;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;

import java.util.ArrayList;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ConcertIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ConcertRepository concertRepository;

    private static final String PATH = "/concerts";

    @Test
    @DisplayName("콘서트 목록이 존재하지 않으면 빈 리스트를 반환한다.")
    void getConcertsTest_empty() {
        // given
        concertRepository.deleteAll();

        // when
        ExtractableResponse<Response> response = get(LOCAL_HOST + port + PATH);

        // then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body().jsonPath().getList("data")).isEqualTo(new ArrayList<>());
    }

    @Test
    @DisplayName("콘서트 목록을 조회하면, 목록을 반환한다.")
    void getConcertsTest_success() {
        // when
        ExtractableResponse<Response> response = get(LOCAL_HOST + port + PATH);

        // then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body().jsonPath().getList("data").size()).isEqualTo(1);
    }

    @Test
    @DisplayName("콘서트 상세 정보가 존재하지 않으면 EntityNotFoundException 예외가 발생한다.")
    void getConcertTest_EntityNotFoundException() {
        // given
        long nonExistentConcertId = 999L; // 존재하지 않는 콘서트 ID

        // when
        get(LOCAL_HOST + port + PATH + "/" + nonExistentConcertId);

        // then
        assertThatThrownBy(() -> {
            throw new EntityNotFoundException();
        });
    }

    @Test
    @DisplayName("콘서트 상세 정보를 조회하면, 정보를 반환한다.")
    void getConcertTest_success() {
        // given
        long concertId = 1L;

        // when
        ExtractableResponse<Response> response = get(LOCAL_HOST + port + PATH + "/" + concertId);

        // then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body().jsonPath().getObject("data", GetConcertResponse.class).name()).isEqualTo("아이유 2024 콘서트");
    }

    @Test
    @DisplayName("예정된 콘서트 날짜가 없으면, DATE_IS_NULL 예외를 반환한다.")
    void getDatesTest_empty() {
        // given
        concertRepository.deleteAllDates();
        long concertId = 1L;

        // when
        get(LOCAL_HOST + port + PATH + "/" + concertId + "/dates");

        // then
        assertThatThrownBy(() -> {
            throw new CustomException(ConcertExceptionEnum.DATE_IS_NULL, null, LogLevel.INFO);
        })
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("예정된 콘서트 날짜가 없습니다.");
    }

    @Test
    @DisplayName("콘서트 날짜 목록을 조회하면, 목록을 반환한다.")
    void getDatesTest_success() {
        // given
        long concertId = 1L;

        // when
        ExtractableResponse<Response> response = get(LOCAL_HOST + port + PATH + "/" + concertId + "/dates");

        // then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body().jsonPath().getObject("data", GetDatesResponse.class).dates().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("콘서트 좌석 목록을 조회하면, 예약가능한 좌석 목록을 반환한다.")
    void getSeatsTest_success() {
        // given
        long concertId = 1L;
        long concertDateId = 1L;

        // when
        ExtractableResponse<Response> response = get(LOCAL_HOST + port + PATH + "/" + concertId + "/dates/" + concertDateId + "/seats");

        // then
        assertThat(response.statusCode()).isEqualTo(200);
        GetSeatsResponse data = response.body().jsonPath().getObject("data", GetSeatsResponse.class);
        assertThat(data.seats().size()).isEqualTo(48);
    }
}
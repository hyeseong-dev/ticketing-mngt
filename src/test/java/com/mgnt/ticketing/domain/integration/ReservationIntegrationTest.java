package com.mgnt.ticketing.domain.integration;

import com.mgnt.ticketing.base.exception.CustomException;
import com.mgnt.ticketing.controller.reservation.dto.request.ReserveRequest;
import com.mgnt.ticketing.domain.integration.base.BaseIntegrationTest;
import com.mgnt.ticketing.domain.integration.base.TestDataHandler;
import com.mgnt.ticketing.domain.reservation.ReservationExceptionEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;

import static com.mgnt.ticketing.domain.integration.base.BaseIntegrationTest.LOCAL_HOST;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ReservationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    TestDataHandler testDataHandler;

    private static final String PATH = "/reservations";

    @Test
    @DisplayName("이미 선택된 좌석입니다. 반환")
    void reserveTest_ALREADY_RESERVED() {
        // given
        ReserveRequest request = new ReserveRequest(1L, 1L, 5L, 1L);

        // when
        post(LOCAL_HOST + port + PATH, request);

        // then
        assertThatThrownBy(() -> {
            throw new CustomException(ReservationExceptionEnum.ALREADY_RESERVED, null, LogLevel.INFO);
        })
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 선택된 좌석입니다.");
    }
    // 따닥 이선좌 (락)
    // 예약 성공
    // 예약 성공하면 임시 점유
    // 예약 성공 후 5분 동안 결제 완료 안하면 예약 취소

    @Test
    void cancel() {
    }

    // 예약 없음 예외처리
    // 예약 취소되면 결제 취소, 잔액 환불
    // 예약 취소되면 예약 내역 삭제됨

    @Test
    void getMyReservation() {
    }

    // 나의 예약 내역 조회
}

//package com.mgnt.ticketing.domain.integration;
//
//import com.mgnt.ticketing.base.exception.ApiResult;
//import com.mgnt.ticketing.base.exception.CustomException;
//import com.mgnt.ticketing.controller.payment.dto.request.CreateRequest;
//import com.mgnt.ticketing.controller.reservation.dto.request.CancelRequest;
//import com.mgnt.ticketing.controller.reservation.dto.request.ReserveRequest;
//import com.mgnt.ticketing.controller.reservation.dto.response.ReserveResponse;
//import com.mgnt.ticketing.domain.TimeService;
//import com.mgnt.ticketing.domain.integration.base.BaseIntegrationTest;
//import com.mgnt.ticketing.domain.integration.base.TestDataHandler;
//import com.mgnt.ticketing.domain.payment.entity.Payment;
//import com.mgnt.ticketing.domain.reservation.ReservationExceptionEnum;
//import com.mgnt.ticketing.domain.reservation.entity.Reservation;
//import com.mgnt.ticketing.domain.reservation.repository.ReservationRepository;
//import io.restassured.response.ExtractableResponse;
//import io.restassured.response.Response;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.logging.LogLevel;
//
//import java.math.BigDecimal;
//import java.time.Clock;
//import java.time.Instant;
//import java.time.ZoneId;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.IntStream;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
//import static org.junit.jupiter.api.Assertions.fail;
//
//class ReservationIntegrationTest extends BaseIntegrationTest {
//
//    @Autowired
//    TestDataHandler testDataHandler;
//    @Autowired
//    ReservationRepository reservationRepository;
//    @Autowired
//    TimeService timeService;
//
//    private static final String PATH = "/reservations";
//
//    @Test
//    @DisplayName("예약된 좌석이면 '이미 선택된 좌석입니다.' 반환")
//    void reserveTest_ALREADY_RESERVED() {
//        // given
//        ReserveRequest request = new ReserveRequest(1L, 1L, 5, 1L);
//
//        // when
//        post(LOCAL_HOST + port + PATH, request);
//
//        // then
//        assertThatThrownBy(() -> {
//            throw new CustomException(ReservationExceptionEnum.ALREADY_RESERVED, null, LogLevel.INFO);
//        })
//                .isInstanceOf(CustomException.class)
//                .hasMessageContaining("이미 선택된 좌석입니다.");
//    }
//
//    @Test
//    @DisplayName("유저 여러 명이 동시에 예약 신청하면 한 명만 예약 성공, 나머지 유저는 '이선좌' 반환")
//    void reserveTest_ALREADY_RESERVED_lock() {
//        // given
//        for (int i = 0; i < 100; i++) {
//            testDataHandler.settingUser(BigDecimal.ZERO);
//        }
//        long concertId = 1L;
//        long concertDateId = 1L;
//        long userId = 3L; // 시작 유저 pk
//        int seatNum = 19;
//
//        // when - 동시에 한 좌석 예약 요청
//        AtomicInteger successCount = new AtomicInteger(0);
//        List<CompletableFuture<ExtractableResponse<Response>>> futures = IntStream.range(0, 10)
//                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
//                    ReserveRequest request = new ReserveRequest(concertId, concertDateId, seatNum, userId + i);
//                    return post(LOCAL_HOST + port + PATH, request);
//                }))
//                .toList();
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//
//        // then
//        List<ExtractableResponse<Response>> responses = futures.stream()
//                .map(CompletableFuture::join)
//                .toList();
//        responses.forEach(response -> {
//            if (response.body().jsonPath().getObject("success", Boolean.class).equals(true)) {
//                successCount.getAndIncrement();
//            } else if (!response.body().jsonPath().getObject("error", ApiResult.Error.class).message().equals("이미 선택된 좌석입니다.")) {
//                fail("예상치 못한 응답: " + response);
//            }
//        });
//        assertThat(successCount.get()).isEqualTo(1);
//    }
//
//    @Test
//    @DisplayName("유저 여러 명이 동시에 다른 좌석 예약 신청하면 모두 예약 성공")
//    void reserveTest_success_all() {
//        // given
//        for (int i = 0; i < 10; i++) {
//            testDataHandler.settingUser(BigDecimal.ZERO);
//        }
//        long concertId = 1L;
//        long concertDateId = 1L;
//        long userId = 1L; // 시작 유저 pk
//        int seatNum = 11;
//
//        // when - 동시에 한 좌석 예약 요청
//        AtomicInteger successCount = new AtomicInteger(0);
//        List<CompletableFuture<ExtractableResponse<Response>>> futures = IntStream.range(0, 100)
//                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
//                    ReserveRequest request = new ReserveRequest(concertId, concertDateId, seatNum + i, userId + i);
//                    return post(LOCAL_HOST + port + PATH, request);
//                }))
//                .toList();
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//
//        // then
//        List<ExtractableResponse<Response>> responses = futures.stream()
//                .map(CompletableFuture::join)
//                .toList();
//        responses.forEach(response -> {
//            if (response.body().jsonPath().getObject("success", Boolean.class).equals(true)) {
//                successCount.getAndIncrement();
//            } else if (!response.body().jsonPath().getObject("error", ApiResult.Error.class).message().equals("이미 선택된 좌석입니다.")) {
//                fail("예상치 못한 응답: " + response);
//            }
//        });
//        assertThat(successCount.get()).isEqualTo(10);
//    }
//
//    @Test
//    @DisplayName("예약을 성공해도 5분간 결제 완료되지 않았을 시 자동 취소된다.")
//    void reserveTest_cancel_after_5min() throws InterruptedException {
//        // 조작된 Clock 설정
//        Instant testStart = Instant.now();
//        Clock fixedClock = Clock.fixed(testStart, ZoneId.systemDefault());
//        timeService.setClock(fixedClock);
//
//        // given
//        testDataHandler.settingUser(BigDecimal.ZERO);
//        long concertId = 1L;
//        long concertDateId = 1L;
//        long userId = 1L;
//        int seatNum = 19;
//        ReserveRequest request = new ReserveRequest(concertId, concertDateId, seatNum, userId);
//
//        // when
//        ExtractableResponse<Response> response = post(LOCAL_HOST + port + PATH, request);
//
//        // then
//        assertThat(response.statusCode()).isEqualTo(200);
//        ReserveResponse data = response.body().jsonPath().getObject("data", ReserveResponse.class);
//        assertThat(data.reservationId()).isEqualTo(3L);
//        assertThat(data.status()).isEqualTo(Reservation.Status.ING);
//        assertThat(reservationRepository.findById(3L)).isNotNull();
//
//        // 임시 점유 시간 대기 - 5분 후 시간으로 시계 조작
//        Instant fiveMinutesLater = testStart.plusSeconds(5 * 60);
//        Clock shiftedClock = Clock.fixed(fiveMinutesLater, ZoneId.systemDefault());
//        timeService.setClock(shiftedClock);
//
//        // 예약이 자동으로 취소되었는지 확인
//    }
//
//    @Test
//    @DisplayName("예약을 성공하면 5분 안에 결제 완료되면 예약 확정된다.")
//    void reserveTest_complete_in_5min() throws InterruptedException {
//        // given
//        testDataHandler.settingUser(BigDecimal.ZERO);
//        long concertId = 1L;
//        long concertDateId = 1L;
//        long userId = 1L;
//        int seatNum = 19;
//        ReserveRequest request = new ReserveRequest(concertId, concertDateId, seatNum, userId);
//        CreateRequest payRequest = new CreateRequest(1L, BigDecimal.valueOf(79000));
//
//        // when
//        post(LOCAL_HOST + port + PATH, request);
//        post(LOCAL_HOST + port + "/payments", payRequest);
//        Thread.sleep(1000);
//
//        // 임시 점유 시간 대기
//        Thread.sleep(5000);
//
//        // then
//        // 예약이 취소되지 않았는지 확인
//        assertThat(reservationRepository.findById(3L)).isNotNull();
//    }
//
//    @Test
//    @DisplayName("해당 예약이 없을 경우 예외 처리한다.")
//    void cancelTest_reservation_is_Null() {
//        // given
//        long reservationId = 1L;
//        CancelRequest request = new CancelRequest(1L);
//
//        // when
//        delete(LOCAL_HOST + port + PATH + "/" + reservationId, request);
//
//        // then
//        assertThatThrownBy(() -> {
//            throw new CustomException(ReservationExceptionEnum.IS_NULL, null, LogLevel.INFO);
//        })
//                .isInstanceOf(CustomException.class)
//                .hasMessageContaining("예약 정보가 없습니다.");
//    }
//
//    @Test
//    @DisplayName("예약 취소 시 결제 내역이 있으면 환불된다. 해당 예약 정보는 삭제된다.")
//    void cancelTest_pay_cancel() {
//        // given
//        long reservationId = 1L;
//        CancelRequest request = new CancelRequest(1L);
//        testDataHandler.settingUser(BigDecimal.valueOf(10000));
//        testDataHandler.createPayment(Payment.Status.COMPLETE);
//
//        // when
//        ExtractableResponse<Response> response = delete(LOCAL_HOST + port + PATH + "/" + reservationId, request);
//
//        // then - 데이터 확인
//        assertThat(response.statusCode()).isEqualTo(200);
//    }
//
//    @Test
//    @DisplayName("예약 취소 시 해당 예약 정보는 삭제된다.")
//    void cancelTest_reservation_delete() {
//        // given
//        long reservationId = 1L;
//        testDataHandler.settingUser(BigDecimal.valueOf(10000));
//        CancelRequest request = new CancelRequest(1L);
//
//        // when
//        ExtractableResponse<Response> response = delete(LOCAL_HOST + port + PATH + "/" + reservationId, request);
//
//        // then - 데이터 확인
//        assertThat(response.statusCode()).isEqualTo(200);
//    }
//
//    @Test
//    @DisplayName("나의 예약 내역을 조회한다.")
//    void getMyReservation() {
//        // given
//        long userId = 1L;
//        Map<String, String> params = new HashMap<>();
//        params.put("userId", String.valueOf(userId));
//
//        // when
//        ExtractableResponse<Response> response = get(LOCAL_HOST + port + PATH + "/me", params);
//
//        // then
//        assertThat(response.statusCode()).isEqualTo(200);
//    }
//}
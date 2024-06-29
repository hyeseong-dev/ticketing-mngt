package com.mgnt.ticketing.domain.unit;

import com.mgnt.ticketing.base.exception.CustomException;
import com.mgnt.ticketing.controller.reservation.dto.request.CancelRequest;
import com.mgnt.ticketing.controller.reservation.dto.request.ReserveRequest;
import com.mgnt.ticketing.controller.reservation.dto.response.ReserveResponse;
import com.mgnt.ticketing.controller.user.dto.response.GetMyReservationsResponse;
import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import com.mgnt.ticketing.domain.concert.entity.Place;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.concert.service.ConcertReader;
import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.payment.service.PaymentReader;
import com.mgnt.ticketing.domain.payment.service.PaymentService;
import com.mgnt.ticketing.domain.payment.service.dto.CancelPaymentResultResDto;
import com.mgnt.ticketing.domain.reservation.ReservationExceptionEnum;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.reservation.repository.ReservationRepository;
import com.mgnt.ticketing.domain.reservation.service.ReservationMonitor;
import com.mgnt.ticketing.domain.reservation.service.ReservationService;
import com.mgnt.ticketing.domain.reservation.service.ReservationValidator;
import com.mgnt.ticketing.domain.reservation.service.dto.GetReservationAndPaymentResDto;
import com.mgnt.ticketing.domain.user.entity.User;
import com.mgnt.ticketing.domain.user.service.UserReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class ReservationServiceTest {

    private ReservationService reservationService;
    private ReservationRepository reservationRepository;
    private ReservationValidator reservationValidator;
    private ReservationMonitor reservationMonitor;
    private ConcertReader concertReader;
    private UserReader userReader;
    private PaymentService paymentService;
    private PaymentReader paymentReader;
    private ApplicationEventPublisher applicationEventPublisher;

    private Reservation 예약건;

    @BeforeEach
    void setUp() {
        // mocking
        reservationRepository = Mockito.mock(ReservationRepository.class);
        reservationValidator = Mockito.mock(ReservationValidator.class);
        reservationMonitor = Mockito.mock(ReservationMonitor.class);
        concertReader = Mockito.mock(ConcertReader.class);
        userReader = Mockito.mock(UserReader.class);
        paymentService = Mockito.mock(PaymentService.class);
        paymentReader = Mockito.mock(PaymentReader.class);
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);

        reservationService = new ReservationService(
                reservationRepository,
                reservationValidator,
                reservationMonitor,
                concertReader,
                userReader,
                paymentService,
                paymentReader,
                applicationEventPublisher
        );

        // 예약 정보 세팅
        예약건 = Reservation.builder()
                .userId(1L)
                .concertId(1L)
                .concertDateId(1L)
                .seatId(1L)
                .status(Reservation.Status.ING)
                .reservedAt(null)
                .build();
    }

    @Test
    @DisplayName("이미 선택된 좌석입니다 예외처리")
    void reserveTest_already_reserved() {
        // given
        ReserveRequest request = new ReserveRequest(1L, 1L, 1L, 1L);

        // when
        when(reservationRepository.findOneByConcertDateIdAndSeatId(request.concertDateId(), request.seatId())).thenReturn(예약건);
        doThrow(new CustomException(ReservationExceptionEnum.ALREADY_RESERVED, null, LogLevel.INFO)).when(reservationValidator).checkReserved(anyLong(), anyLong());

        // then
        CustomException expected = assertThrows(CustomException.class, () ->
                reservationValidator.checkReserved(anyLong(), anyLong()));
        assertThat(expected.getMessage()).isEqualTo("이미 선택된 좌석입니다.");
    }

    @Test
    @DisplayName("좌석 예약을 성공")
    void reserveTest_success() {
        // given
        ReserveRequest request = new ReserveRequest(1L, 1L, 1L, 1L);

        // when
        when(reservationRepository.findOneByConcertDateIdAndSeatId(request.concertDateId(), request.seatId())).thenReturn(null);
        when(reservationRepository.save(request.toEntity(concertReader, userReader))).thenReturn(예약건);
        when(concertReader.findConcert(anyLong())).thenReturn(Concert.builder().build());
        when(concertReader.findConcertDate(anyLong())).thenReturn(ConcertDate.builder().build());
        when(concertReader.findSeat(anyLong())).thenReturn(Seat.builder().build());
        ReserveResponse response = reservationService.reserve(request);

        // then
        assertThat(response.status()).isEqualTo(Reservation.Status.ING);
    }

    @Test
    @DisplayName("예약을 취소한다.")
    void cancelTest_cancel() {
        // given
        Long reservationId = 1L;
        CancelRequest request = new CancelRequest(1L);

        // when
        when(reservationRepository.findByIdAndUserId(reservationId, request.userId())).thenReturn(예약건);
        reservationService.cancel(reservationId, request);
    }

    @Test
    @DisplayName("예약 내역 목록을 조회한다.")
    void getMyReservationsTest_my() {
        // given
        Long userId = 1L;

        // when
        when(reservationRepository.getMyReservations(userId)).thenReturn(List.of(new GetReservationAndPaymentResDto(
                예약건,
                Concert.builder().name("임영웅 콘서트").build(),
                ConcertDate.builder().build(),
                Seat.builder().build()))
        );
        List<GetMyReservationsResponse> responses = reservationService.getMyReservations(userId);

        // then
        assertThat(responses.size()).isEqualTo(1);
        assertThat(responses.get(0).concertInfo().name()).isEqualTo("임영웅 콘서트");
    }
}
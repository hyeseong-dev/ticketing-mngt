package com.mgnt.ticketing.domain.unit;

import com.mgnt.ticketing.base.constant.UserRoleEnum;
import com.mgnt.ticketing.base.exception.CustomException;
import com.mgnt.ticketing.controller.payment.dto.request.PayRequest;
import com.mgnt.ticketing.controller.payment.dto.response.PayResponse;
import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import com.mgnt.ticketing.domain.concert.entity.Place;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.payment.PaymentExceptionEnum;
import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.payment.repository.PaymentRepository;
import com.mgnt.ticketing.domain.payment.service.PaymentService;
import com.mgnt.ticketing.domain.payment.service.PaymentValidator;
import com.mgnt.ticketing.domain.payment.service.dto.CancelPaymentResultResDto;
import com.mgnt.ticketing.domain.payment.service.dto.CreatePaymentReqDto;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.user.entity.User;
import com.mgnt.ticketing.domain.user.service.UserReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.logging.LogLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.doThrow;

class PaymentServiceTest {

    private PaymentService paymentService;
    private PaymentRepository paymentRepository;
    private PaymentValidator paymentValidator;
    private UserReader userReader;

    private Reservation 예약건;

    @BeforeEach
    void setUp() {
        // mocking
        paymentRepository = Mockito.mock(PaymentRepository.class);
        paymentValidator = Mockito.mock(PaymentValidator.class);
        userReader = Mockito.mock(UserReader.class);

        paymentService = new PaymentService(
                paymentRepository,
                paymentValidator,
                userReader
        );

        // 예약 정보 세팅
        예약건 = Reservation.builder()
                .user(new User(1L, BigDecimal.valueOf(100000)))
                .concert(new Concert(
                        "임영웅 콘서트",
                        1L,
                        List.of(new ConcertDate(1L,
                                ZonedDateTime.of(
                                        LocalDateTime.of(2024, 5, 25, 18, 30, 0),
                                        ZoneId.of("Asia/Seoul"))))))
                .concertDate(new ConcertDate(1L,
                        ZonedDateTime.of(
                                LocalDateTime.of(2024, 5, 25, 18, 30, 0),
                                ZoneId.of("Asia/Seoul"))))
                .seat(new Seat(1L, Place.builder().build(), 1, BigDecimal.valueOf(79000)))
                .status(Reservation.Status.ING)
                .reservedAt(null)
                .build();
    }

    @Test
    @DisplayName("결제_요청_불가능한_상태")
    void payTest_결제_요청_불가능한_상태() {
        // given
        Long paymentId = 1L;
        Payment 완료된_결제건 = Payment.builder()
                .reservation(예약건)
                .status(Payment.Status.COMPLETE)
                .price(BigDecimal.valueOf(79000))
                .build();

        // when
        when(paymentRepository.findById(paymentId)).thenReturn(완료된_결제건);
        doThrow(new CustomException(PaymentExceptionEnum.NOT_AVAILABLE_PAY, null, LogLevel.INFO)).when(paymentValidator).checkPayStatus(any());

        // then
        CustomException expected = assertThrows(CustomException.class, () ->
                paymentValidator.checkPayStatus(any()));
        assertThat(expected.getMessage()).isEqualTo("결제 가능한 상태가 아닙니다.");
    }

    @Test
    @DisplayName("결제_잔액_부족하여_실패")
    void payTest_결제_잔액_부족하여_실패() {
        // given
        Long paymentId = 1L;
        PayRequest request = new PayRequest(1L);
        Payment 결제건 = Payment.builder()
                .reservation(예약건)
                .status(Payment.Status.READY)
                .price(BigDecimal.valueOf(79000))
                .build();
        User 사용자 = new User(1L, BigDecimal.valueOf(10000));

        // when
        when(paymentRepository.findById(paymentId)).thenReturn(결제건);
        when(userReader.findUser(request.userId())).thenReturn(사용자);
        doThrow(new CustomException(PaymentExceptionEnum.INSUFFICIENT_BALANCE, null, LogLevel.INFO)).when(paymentValidator).checkBalance(결제건.getPrice(), 사용자.getBalance());

        // then
        CustomException expected = assertThrows(CustomException.class, () ->
                paymentValidator.checkBalance(결제건.getPrice(), 사용자.getBalance()));
        assertThat(expected.getMessage()).isEqualTo("잔액이 부족합니다.");
    }

    @Test
    @DisplayName("결제_요청_처리_성공")
    void payTest_결제_요청_처리_성공() {
        // given
        Long paymentId = 1L;
        PayRequest request = new PayRequest(1L);
        Payment 결제건 = Payment.builder()
                .reservation(예약건)
                .status(Payment.Status.READY)
                .price(BigDecimal.valueOf(79000))
                .build();
        User 사용자 = new User(1L, BigDecimal.valueOf(100000));

        // when
        when(paymentRepository.findById(paymentId)).thenReturn(결제건);
        when(userReader.findUser(request.userId())).thenReturn(사용자);
        PayResponse response = paymentService.pay(paymentId, request);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.status()).isEqualTo(Payment.Status.COMPLETE);
        assertThat(response.balance()).isEqualTo(BigDecimal.valueOf(21000));
    }

    @Test
    @DisplayName("결제_생성")
    void createTest_결제_생성() {
        // given
        CreatePaymentReqDto reqDto = new CreatePaymentReqDto(
                예약건,
                Payment.Status.READY,
                BigDecimal.valueOf(79000)
        );

        // when
        when(paymentRepository.save(reqDto.toEntity())).thenReturn(Payment.builder()
                .reservation(reqDto.reservation())
                .status(reqDto.status())
                .price(reqDto.price())
                .build());
        Payment response = paymentService.create(reqDto);

        // then
        assertThat(response.getReservation()).isEqualTo(예약건);
        assertThat(response.getStatus()).isEqualTo(Payment.Status.READY);
    }

    @Test
    @DisplayName("결제_대기건은_즉시취소")
    void cancelTest_결제_대기건은_즉시취소() {
        // given
        Long paymentId = 1L;
        Payment 결제대기건 = Payment.builder()
                .reservation(예약건)
                .status(Payment.Status.READY)
                .price(BigDecimal.valueOf(79000))
                .build();

        // when
        when(paymentRepository.findById(paymentId)).thenReturn(결제대기건);
        CancelPaymentResultResDto response = paymentService.cancel(paymentId);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.status()).isEqualTo(Payment.Status.CANCEL);
    }

    @Test
    @DisplayName("결제_완료건은_환불")
    void cancelTest_결제_완료건은_환불() {
        // given
        Long paymentId = 1L;
        Payment 결제완료건 = Payment.builder()
                .reservation(예약건)
                .status(Payment.Status.COMPLETE)
                .price(BigDecimal.valueOf(79000))
                .build();

        // when
        when(paymentRepository.findById(paymentId)).thenReturn(결제완료건);
        CancelPaymentResultResDto response = paymentService.cancel(paymentId);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.status()).isEqualTo(Payment.Status.REFUND);
    }

    @Test
    @DisplayName("결제_취소_불가능한_상태")
    void cancelTest_결제_취소_불가능한_상태() {
        // given
        Long paymentId = 1L;
        Payment 결제취소건 = Payment.builder()
                .reservation(예약건)
                .status(Payment.Status.CANCEL)
                .price(BigDecimal.valueOf(79000))
                .build();

        // when
        when(paymentRepository.findById(paymentId)).thenReturn(결제취소건);
        doThrow(new CustomException(PaymentExceptionEnum.NOT_AVAILABLE_CANCEL, null, LogLevel.INFO)).when(paymentValidator).checkCancelStatus(결제취소건.getStatus());

        // then
        CustomException expected = assertThrows(CustomException.class, () ->
                paymentValidator.checkCancelStatus(결제취소건.getStatus()));
        assertThat(expected.getMessage()).isEqualTo("취소 가능한 상태가 아닙니다.");
    }
}
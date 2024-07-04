package com.mgnt.ticketing.domain.unit;

import com.mgnt.ticketing.base.exception.CustomException;
import com.mgnt.ticketing.controller.payment.dto.request.CreateRequest;
import com.mgnt.ticketing.controller.payment.dto.request.PayRequest;
import com.mgnt.ticketing.controller.payment.dto.response.CreateResponse;
import com.mgnt.ticketing.controller.payment.dto.response.PayResponse;
import com.mgnt.ticketing.domain.payment.PaymentExceptionEnum;
import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.payment.repository.PaymentRepository;
import com.mgnt.ticketing.domain.payment.service.PaymentService;
import com.mgnt.ticketing.domain.payment.service.PaymentValidator;
import com.mgnt.ticketing.domain.payment.service.dto.CancelPaymentResultResDto;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.reservation.service.ReservationReader;
import com.mgnt.ticketing.domain.user.entity.Users;
import com.mgnt.ticketing.domain.user.service.UserReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.logging.LogLevel;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.doThrow;

class PaymentServiceTest {

    private PaymentService paymentService;
    private PaymentRepository paymentRepository;
    private PaymentValidator paymentValidator;
    private UserReader userReader;
    private ReservationReader reservationReader;

    private Reservation 예약건;

    @BeforeEach
    void setUp() {
        // mocking
        paymentRepository = Mockito.mock(PaymentRepository.class);
        paymentValidator = Mockito.mock(PaymentValidator.class);
        userReader = Mockito.mock(UserReader.class);
        reservationReader = Mockito.mock(ReservationReader.class);

        paymentService = new PaymentService(
                paymentRepository,
                paymentValidator,
                userReader,
                reservationReader
        );

        // 예약 정보 세팅
        예약건 = Reservation.builder()
                .userId(1L)
                .concertId(1L)
                .concertDateId(1L)
                .seatNum(5)
                .status(Reservation.Status.ING)
                .reservedAt(null)
                .build();
    }

    @Test
    @DisplayName("결제 요청이 불가능한 상태")
    void payTest_status_disable() {
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
    @DisplayName("결제 시 잔액이 부족하면 실패된다.")
    void payTest_lack_balance() {
        // given
        Long paymentId = 1L;
        PayRequest request = new PayRequest(1L);
        Payment 결제건 = Payment.builder()
                .reservation(예약건)
                .status(Payment.Status.READY)
                .price(BigDecimal.valueOf(79000))
                .build();
        Users 사용자 = new Users(1L, BigDecimal.valueOf(10000));

        // when
        when(paymentRepository.findById(paymentId)).thenReturn(결제건);
        when(userReader.findUser(request.userId())).thenReturn(사용자);
        doThrow(new CustomException(PaymentExceptionEnum.INSUFFICIENT_BALANCE, null, LogLevel.INFO))
                .when(paymentValidator).checkBalance(결제건.getPrice(), 사용자.getBalance());

        // then
        CustomException expected = assertThrows(CustomException.class, () ->
                paymentValidator.checkBalance(결제건.getPrice(), 사용자.getBalance()));
        assertThat(expected.getMessage()).isEqualTo("잔액이 부족합니다.");
    }

    @Test
    @DisplayName("결제 요청 처리됨")
    void payTest_success() {
        // given
        Long paymentId = 1L;
        PayRequest request = new PayRequest(1L);
        Payment 결제건 = Payment.builder()
                .reservation(예약건)
                .status(Payment.Status.READY)
                .price(BigDecimal.valueOf(79000))
                .build();
        Users 사용자 = new Users(1L, BigDecimal.valueOf(100000));

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
    @DisplayName("결제를 생성한다.")
    void createTest_create() {
        // given
        CreateRequest reqDto = new CreateRequest(
                1L,
                BigDecimal.valueOf(79000)
        );

        // when
        when(paymentRepository.save(reqDto.toEntity(예약건))).thenReturn(Payment.builder()
                .paymentId(1L)
                .reservation(예약건)
                .status(Payment.Status.READY)
                .price(reqDto.price())
                .build());
        CreateResponse response = paymentService.create(reqDto);

        // then
        assertThat(response.paymentId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("취소 시 결제 대기건은 즉시취소된다.")
    void cancelTest_direct_cancel() {
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
    @DisplayName("취소 시 결제 완료건은 환불된다.")
    void cancelTest_refund() {
        // given
        Long paymentId = 1L;
        Payment 결제완료건 = Payment.builder()
                .reservation(예약건)
                .status(Payment.Status.COMPLETE)
                .price(BigDecimal.valueOf(79000))
                .build();

        // when
        when(paymentRepository.findById(paymentId)).thenReturn(결제완료건);
        when(userReader.findUser(anyLong())).thenReturn(new Users(1L, BigDecimal.valueOf(10000)));
        CancelPaymentResultResDto response = paymentService.cancel(paymentId);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.status()).isEqualTo(Payment.Status.REFUND);
    }

    @Test
    @DisplayName("결제 취소 불가능한 상태")
    void cancelTest_not_available() {
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
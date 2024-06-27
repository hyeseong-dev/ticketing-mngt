package com.mgnt.ticketing.domain.reservation.service;

import com.mgnt.ticketing.controller.reservation.dto.request.CancelRequest;
import com.mgnt.ticketing.controller.reservation.dto.request.ReserveRequest;
import com.mgnt.ticketing.controller.reservation.dto.response.ReserveResponse;
import com.mgnt.ticketing.controller.user.dto.response.GetMyReservationsResponse;
import com.mgnt.ticketing.domain.concert.service.ConcertReader;
import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.payment.service.PaymentReader;
import com.mgnt.ticketing.domain.payment.service.PaymentService;
import com.mgnt.ticketing.domain.payment.service.dto.CancelPaymentResultResDto;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.reservation.repository.ReservationRepository;
import com.mgnt.ticketing.domain.reservation.service.dto.GetReservationAndPaymentResDto;
import com.mgnt.ticketing.domain.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 예약 서비스 클래스
 *
 * 이 클래스는 예약과 관련된 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
public class ReservationService implements ReservationInterface {

    private final ReservationRepository reservationRepository;
    private final ReservationValidator reservationValidator;
    private final ConcertReader concertReader;
    private final UserReader userReader;
    private final PaymentService paymentService;
    private final PaymentReader paymentReader;

    /**
     * 콘서트 좌석 예매
     *
     * @param request 예매 요청 DTO
     * @return 예매 응답 DTO
     */
    @Override
    public ReserveResponse reserve(ReserveRequest request) {
        // 예약 유효성 검사
        reservationValidator.checkReserved(request.concertDateId(), request.seatId());

        // 좌석 예약
        Reservation reservation = reservationRepository.save(request.toEntity(concertReader, userReader));
        // 결제 정보 생성
        Payment payment = paymentService.create(reservation.toCreatePayment());
        // TODO - 5분 선점 로직

        return ReserveResponse.from(reservation, payment);
    }

    /**
     * 좌석 예매 취소
     *
     * @param reservationId 예약 ID
     * @param request 예매 취소 요청 DTO
     */
    @Override
    public void cancel(Long reservationId, CancelRequest request) {
        Reservation reservation = reservationRepository.findByIdAndUserId(reservationId, request.userId());

        // 예약 유효성 검사
        reservationValidator.isNull(reservation);

        // 취소 처리
        // 1. 결제 정보 처리
        Payment payment = paymentReader.findPaymentByReservation(reservation);
        CancelPaymentResultResDto cancelPaymentInfo = paymentService.cancel(payment.getPaymentId());
        if (cancelPaymentInfo.isSuccess()) { // 결제 정보 처리 성공 시
            // 2. 예약 내역 삭제
            reservationRepository.delete(reservation);
        }
    }

    /**
     * 나의 예약 내역 조회
     *
     * @param userId 사용자 ID
     * @return 예약 내역 응답 DTO 리스트
     */
    @Override
    public List<GetMyReservationsResponse> getMyReservations(Long userId) {
        // 사용자의 예약 및 결제 정보 조회
        List<GetReservationAndPaymentResDto> reservationsAndPayments = reservationRepository.getMyReservations(userId);
        return reservationsAndPayments.stream().map(GetMyReservationsResponse::from).toList();
    }
}

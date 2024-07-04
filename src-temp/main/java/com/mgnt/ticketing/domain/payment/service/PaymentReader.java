package com.mgnt.ticketing.domain.payment.service;

import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.payment.repository.PaymentRepository;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 결제 정보 조회 서비스
 *
 * 이 클래스는 결제와 관련된 정보를 단순 조회하는 기능을 제공합니다.
 */
@Component
@RequiredArgsConstructor
public class PaymentReader {
    /* Payment 관련 정보 단순 조회용 */

    private final PaymentRepository paymentRepository;

    /**
     * 예약 건으로 결제 정보 조회
     *
     * @param reservation 예약 정보
     * @return 결제 정보
     */
    public Payment findPaymentByReservation(Reservation reservation) {
        return paymentRepository.findByReservation(reservation);
    }
}

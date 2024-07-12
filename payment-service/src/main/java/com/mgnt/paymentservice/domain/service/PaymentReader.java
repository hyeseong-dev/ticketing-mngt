//package com.mgnt.paymentservice.domain.service;
//
//import com.mgnt.paymentservice.domain.entity.Payment;
//import com.mgnt.paymentservice.domain.repository.PaymentRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//
///**
// * 결제 정보 조회 서비스
// * <p>
// * 이 클래스는 결제와 관련된 정보를 단순 조회하는 기능을 제공합니다.
// */
//@Component
//@RequiredArgsConstructor
//public class PaymentReader {
//    /* Payment 관련 정보 단순 조회용 */
//
//    private final PaymentRepository paymentRepository;
//
//    /**
//     * 예약 건으로 결제 정보 조회
//     *
//     * @param reservationId 예약 정보
//     * @return 결제 정보
//     */
//    public Payment findPaymentByReservation(Long reservationId) {
//        return paymentRepository.findByReservationId(reservationId);
//    }
//}

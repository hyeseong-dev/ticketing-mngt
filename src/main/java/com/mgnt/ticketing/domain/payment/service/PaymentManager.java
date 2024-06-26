package com.mgnt.ticketing.domain.payment.service;


import com.mgnt.ticketing.domain.payment.PaymentEnums;
import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.payment.repository.PaymentRepository;
import com.mgnt.ticketing.domain.payment.service.dto.CancelPaymentResultResDto;
import com.mgnt.ticketing.domain.payment.service.dto.CreatePaymentReqDto;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class PaymentManager {

    private final PaymentRepository paymentRepository;

    // 결제 정보 생성
    public Payment create(CreatePaymentReqDto reqDto) {
        return paymentRepository.save(reqDto.toEntity());
    }

    // 결제 취소
    public CancelPaymentResultResDto cancel(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentEnums.Status toBeStatus = null;
        if (PaymentEnums.Status.READY.equals(payment.getStatus())) {
            // 결제 대기 상태일 경우 - 즉시 취소
            toBeStatus = PaymentEnums.Status.CANCEL;
        } else if (PaymentEnums.Status.COMPLETE.equals(payment.getStatus())) {
            // 결제 완료 상태일 경우 - 환불
            toBeStatus = PaymentEnums.Status.REFUND;
        }

        Payment updatedPayment = payment.updateStatus(toBeStatus);
        boolean isSuccess = updatedPayment != null;
        if (isSuccess) {
            return new CancelPaymentResultResDto(true, updatedPayment.getPaymentId(), updatedPayment.getStatus());
        } else {
            return new CancelPaymentResultResDto(false, payment.getPaymentId(), payment.getStatus());
        }
    }

    // 예약으로 결제 건 조회
    public Payment getPaymentByReservation(Reservation reservation) {
        return paymentRepository.findByReservation(reservation);
    }
}


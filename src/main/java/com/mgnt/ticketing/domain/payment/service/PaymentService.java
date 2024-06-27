package com.mgnt.ticketing.domain.payment.service;

import com.mgnt.ticketing.controller.payment.dto.request.PayRequest;
import com.mgnt.ticketing.controller.payment.dto.response.PayResponse;
import com.mgnt.ticketing.domain.payment.PaymentEnums;
import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.payment.repository.PaymentRepository;
import com.mgnt.ticketing.domain.payment.service.dto.CancelPaymentResultResDto;
import com.mgnt.ticketing.domain.payment.service.dto.CreatePaymentReqDto;
import com.mgnt.ticketing.domain.user.entity.User;
import com.mgnt.ticketing.domain.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 결제 서비스 클래스
 *
 * 이 클래스는 결제와 관련된 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
public class PaymentService implements PaymentInterface {

    private final PaymentRepository paymentRepository;
    private final PaymentValidator paymentValidator;
    private final UserReader userReader;

    /**
     * 결제 요청
     *
     * @param paymentId 결제 ID
     * @param request 결제 요청 DTO
     * @return 결제 응답 DTO
     */
    @Override
    public PayResponse pay(Long paymentId, PayRequest request) {
        // 결제 상태 검증
        Payment payment = paymentRepository.findById(paymentId);
        paymentValidator.checkPayStatus(payment.getStatus());

        // 사용자 잔액 검증
        User user = userReader.findUser(request.userId());
        paymentValidator.checkBalance(payment.getPrice(), user.getBalance());

        // 결제
        boolean isSuccess = false;
        Payment paymentResult = payment.applyPay();
        BigDecimal usedBalance = user.getBalance();
        if (paymentResult.getStatus().equals(PaymentEnums.Status.COMPLETE)) {
            // 사용자 잔액 차감
            usedBalance = user.useBalance(payment.getPrice());
            isSuccess = true;
        }

        return PayResponse.from(isSuccess, paymentResult, usedBalance);
    }

    /**
     * 결제 정보 생성
     *
     * @param reqDto 결제 생성 요청 DTO
     * @return 생성된 결제 정보
     */
    @Override
    public Payment create(CreatePaymentReqDto reqDto) {
        return paymentRepository.save(reqDto.toEntity());
    }

    /**
     * 결제 취소
     *
     * @param paymentId 결제 ID
     * @return 결제 취소 결과 응답 DTO
     */
    @Override
    public CancelPaymentResultResDto cancel(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId);

        // 결제 취소 상태 검증
        paymentValidator.checkCancelStatus(payment.getStatus());

        // 결제 취소
        Payment updatedPayment = cancelPayment(payment);

        // 성공 / 실패 응답 반환
        boolean isSuccess = updatedPayment != null;
        if (isSuccess) {
            return new CancelPaymentResultResDto(true, updatedPayment.getPaymentId(), updatedPayment.getStatus());
        } else {
            return new CancelPaymentResultResDto(false, payment.getPaymentId(), payment.getStatus());
        }
    }

    /**
     * 결제 취소 처리
     *
     * @param payment 결제 정보
     * @return 취소된 결제 정보
     */
    private Payment cancelPayment(Payment payment) {
        Payment updatedPayment = payment;
        User user = payment.getReservation().getUser();

        if (PaymentEnums.Status.READY.equals(payment.getStatus())) {
            // 결제 대기 상태일 경우 - 즉시 취소
            updatedPayment = payment.updateStatus(PaymentEnums.Status.CANCEL);
        } else if (PaymentEnums.Status.COMPLETE.equals(payment.getStatus())) {
            // 결제 완료 상태일 경우 - 환불 처리
            updatedPayment = payment.updateStatus(PaymentEnums.Status.REFUND);
            // 사용자 잔액 환불
            user.refundBalance(payment.getPrice());
        }

        return updatedPayment;
    }
}

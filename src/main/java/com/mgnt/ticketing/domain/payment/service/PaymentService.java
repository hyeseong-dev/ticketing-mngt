package com.mgnt.ticketing.domain.payment.service;

import com.mgnt.ticketing.controller.payment.dto.request.PayRequest;
import com.mgnt.ticketing.controller.payment.dto.response.PayResponse;
import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.payment.repository.PaymentRepository;
import com.mgnt.ticketing.domain.payment.service.dto.CancelPaymentResultResDto;
import com.mgnt.ticketing.domain.payment.service.dto.CreatePaymentReqDto;
import com.mgnt.ticketing.domain.user.entity.User;
import com.mgnt.ticketing.domain.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 결제 서비스 클래스
 *
 * 이 클래스는 결제와 관련된 비즈니스 로직을 처리합니다.
 */
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
     * 결제 요청을 처리합니다.
     *
     * @param paymentId 결제 ID
     * @param request 결제 요청 객체
     * @return 결제 응답 객체
     */
    @Override
    @Transactional
    public PayResponse pay(Long paymentId, PayRequest request) {
        // validator - 결제 상태 검증
        Payment payment = paymentRepository.findById(paymentId);
        paymentValidator.checkPayStatus(payment.getStatus());

        // validator - 사용자 잔액 검증
        User user = userReader.findUser(request.userId());
        paymentValidator.checkBalance(payment.getPrice(), user.getBalance());

        // 결제 요청
        boolean isSuccess = false;
        // 1. 사용자 잔액 차감
        BigDecimal previousBalance = user.getBalance();
        BigDecimal usedBalance = user.useBalance(payment.getPrice());
        if (usedBalance.equals(previousBalance.subtract(payment.getPrice()))) {
            // 2-1. 결제 완료 처리
            payment = payment.toPaid();
            payment.getReservation().toComplete();
            isSuccess = true;
        } else {
            // 2-2. 결제 실패 : 잔액 원복
            usedBalance = user.getBalance();
        }

        return PayResponse.from(isSuccess, payment, usedBalance);
    }

    /**
     * 결제 정보를 생성합니다.
     *
     * @param reqDto 결제 요청 DTO
     * @return 생성된 결제 객체
     */
    @Override
    public Payment create(CreatePaymentReqDto reqDto) {
        return paymentRepository.save(reqDto.toEntity());
    }

    /**
     * 결제를 취소합니다.
     *
     * @param paymentId 결제 ID
     * @return 결제 취소 결과 응답 DTO
     */
    @Override
    @Transactional
    public CancelPaymentResultResDto cancel(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId);

        // validator
        paymentValidator.checkCancelStatus(payment.getStatus());

        // 취소
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
     * 결제를 취소하는 내부 메서드입니다.
     *
     * @param payment 결제 객체
     * @return 취소된 결제 객체
     */
    private Payment cancelPayment(Payment payment) {
        Payment updatedPayment = payment;
        User user = payment.getReservation().getUser();

        if (Payment.Status.READY.equals(payment.getStatus())) {
            // 결제 대기 상태 - 즉시 취소
            updatedPayment = payment.updateStatus(Payment.Status.CANCEL);
        } else if (Payment.Status.COMPLETE.equals(payment.getStatus())) {
            // 결제 완료 상태 - 환불
            updatedPayment = payment.updateStatus(Payment.Status.REFUND);
            user.refundBalance(payment.getPrice());
        }

        return updatedPayment;
    }
}

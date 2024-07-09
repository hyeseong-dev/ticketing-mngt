package com.mgnt.paymentservice.domain.service;

import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.event.*;

import com.mgnt.core.exception.CustomException;
import com.mgnt.paymentservice.controller.dto.request.CreateRequest;
import com.mgnt.paymentservice.controller.dto.request.PayRequest;
import com.mgnt.paymentservice.controller.dto.response.CreateResponse;
import com.mgnt.paymentservice.controller.dto.response.PayResponse;
import com.mgnt.paymentservice.domain.entity.Payment;
import com.mgnt.paymentservice.domain.repository.PaymentRepository;
import com.mgnt.paymentservice.domain.service.dto.CancelPaymentResultResDto;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Event> kafkaTemplate;
    private final PaymentValidator paymentValidator;
//    private final UserReader userReader;
//    private final ReservationReader reservationReader;

    @KafkaListener(topics = "payments-created")
    public void handlePaymentCreated(PaymentCreatedEvent event) {
        processPayment(event.paymentId());
    }

    @Transactional
    void processPayment(Long paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId).orElseThrow(
                () -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND, null, Level.ERROR));

        // 사용자 잔액 확인 요청
        kafkaTemplate.send("user-balance-check-requests", new UserBalanceCheckRequestEvent(payment.getUserId(), paymentId));
    }

    @KafkaListener(topics = "user-balance-check-response")
    public void handleUserBalanceCheckResponse(UserBalanceCheckResponseEvent event) {
        Payment payment = paymentRepository.findByPaymentId(event.paymentId()).orElseThrow(
                () -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND, null, Level.ERROR));

        if (event.balance().compareTo(payment.getPrice()) >= 0) {
            // 잔액이 충분한 경우
            kafkaTemplate.send("user-balance-update-requests",
                    new UserBalanceUpdateEvent(payment.getUserId(), event.paymentId(), payment.getPrice()));
        } else {
            // 잔액이 부족한 경우
            completePayment(payment, false);
        }
    }

    @KafkaListener(topics = "user-balance-update-response")
    public void handleUserBalanceUpdateResponse(UserBalanceUpdateResponseEvent event) {
        Payment payment = paymentRepository.findByPaymentId(event.paymentId()).orElseThrow(
                () -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND, null, Level.ERROR));
        completePayment(payment, event.success());
    }


    private void completePayment(Payment payment, boolean success) {
        payment.setStatus(success ? Payment.Status.COMPLETE : Payment.Status.FAILED);
        payment = paymentRepository.save(payment);

        kafkaTemplate.send("payment-completed", new PaymentCompletedEvent(
                payment.getPaymentId(),
                payment.getReservationId(),
                payment.getUserId(),
                payment.getPrice(),
                success,
                payment.getPrice()
        ));

    }

    @KafkaListener(topics = "reservations-created")
    public void handleReservationCreated(ReservationCreatedEvent event) {
        Payment payment = Payment.builder()
                .reservationId(event.reservationId())
                .status(Payment.Status.READY)
                .price(event.price())
                .build();
        payment = paymentRepository.save(payment);

        PaymentCreatedEvent paymentCreatedEvent = new PaymentCreatedEvent(
                payment.getPaymentId(), payment.getReservationId(), payment.getPrice());
        kafkaTemplate.send("payments-created", paymentCreatedEvent);
    }


//    @Override
//    @Transactional
//    public PayResponse pay(Long paymentId, PayRequest request) {
//        // validator - 결제 상태 검증
//        Payment payment = paymentRepository.findById(paymentId);
//        paymentValidator.checkPayStatus(payment.getStatus());
//
//        // validator - 사용자 잔액 검증
//        Users user = userReader.findUser(request.userId());
//        paymentValidator.checkBalance(payment.getPrice(), user.getBalance());
//
//        // 결제 요청
//        boolean isSuccess = false;
//        // 1. 사용자 잔액 차감
//        BigDecimal previousBalance = user.getBalance();
//        BigDecimal usedBalance = user.useBalance(payment.getPrice());
//        if (usedBalance.equals(previousBalance.subtract(payment.getPrice()))) {
//            // 2-1. 결제 완료 처리
//            payment = payment.toPaid();
//            payment.getReservation().toComplete();
//            isSuccess = true;
//        } else {
//            // 2-2. 결제 실패 : 잔액 원복
//            usedBalance = user.getBalance();
//        }
//
//        return PayResponse.from(isSuccess, payment, usedBalance);
//    }
//
//    @Transactional
//    public CreateResponse create(CreateRequest request) {
//        Reservation reservation = reservationReader.findReservation(request.reservationId());
//        Payment payment = paymentRepository.save(request.toEntity(reservation));
//        if (payment == null) {
//            return new CreateResponse(null);
//        }
//        return new CreateResponse(payment.getPaymentId());
//    }
//
//
//    @Override
//    @Transactional
//    public CancelPaymentResultResDto cancel(Long paymentId) {
//        Payment payment = paymentRepository.findById(paymentId);
//
//        // validator
//        paymentValidator.checkCancelStatus(payment.getStatus());
//
//        // 취소
//        Payment updatedPayment = cancelPayment(payment);
//
//        // 성공 / 실패 응답 반환
//        boolean isSuccess = updatedPayment != null;
//        if (isSuccess) {
//            return new CancelPaymentResultResDto(true, updatedPayment.getPaymentId(), updatedPayment.getStatus());
//        } else {
//            return new CancelPaymentResultResDto(false, payment.getPaymentId(), payment.getStatus());
//        }
//    }
//
//
//    private Payment cancelPayment(Payment payment) {
//        Payment updatedPayment = payment;
//        Long userId = payment.getReservation().getUserId();
//        Users user = userReader.findUser(userId);
//
//        if (Payment.Status.READY.equals(payment.getStatus())) {
//            // 결제 대기 상태 - 즉시 취소
//            updatedPayment = payment.updateStatus(Payment.Status.CANCEL);
//        } else if (Payment.Status.COMPLETE.equals(payment.getStatus())) {
//            // 결제 완료 상태 - 환불
//            updatedPayment = payment.updateStatus(Payment.Status.REFUND);
//            user.refundBalance(payment.getPrice());
//        }
//
//        return updatedPayment;
//    }
}

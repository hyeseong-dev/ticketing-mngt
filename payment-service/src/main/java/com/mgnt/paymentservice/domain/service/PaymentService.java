package com.mgnt.paymentservice.domain.service;

import com.mgnt.core.enums.PaymentStatus;
import com.mgnt.core.enums.ReservationStatus;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.event.*;

import com.mgnt.core.exception.CustomException;
import com.mgnt.paymentservice.controller.dto.request.CreateRequest;
import com.mgnt.paymentservice.controller.dto.request.PayRequest;
import com.mgnt.paymentservice.controller.dto.response.CreateResponse;
//import com.mgnt.paymentservice.controller.dto.response.PayResponse;
import com.mgnt.paymentservice.domain.entity.Payment;
import com.mgnt.paymentservice.domain.repository.PaymentRepository;
import com.mgnt.paymentservice.domain.service.dto.CancelPaymentResultResDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentValidator paymentValidator;
//    private final UserReader userReawder;
//    private final ReservationReader reservationReader;

//    @KafkaListener(topics = "payments-created")
//    @Transactional
//    public void handlePaymentCreated(PaymentCreatedEvent event) {
//        processPayment(event.paymentId());
//    }
//
//    @Transactional
//    void processPayment(Long paymentId) {
//        Payment payment = paymentRepository.findByPaymentId(paymentId).orElseThrow(
//                () -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND, null, Level.ERROR));
//
//        // 사용자 잔액 확인 요청
//        kafkaTemplate.send("user-balance-check-requests", new UserBalanceCheckRequestEvent(payment.getUserId(), paymentId));
//    }

    @KafkaListener(topics = "user-balance-check-responses")
    @Transactional
    public void handleUserBalanceCheckResponse(UserBalanceCheckResponseEvent event) {
        try {
            Payment payment = paymentRepository.findById(event.paymentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND, null, Level.WARN));

            if (event.balance().compareTo(payment.getPrice()) >= 0) {
                kafkaTemplate.send("user-balance-update-requests", new UserBalanceUpdateEvent(
                        payment.getUserId(), event.paymentId(), payment.getPrice()));
            } else {
                completePayment(payment, false);
            }
        } catch (Exception e) {
            log.error("Error handling user balance check response", e);
            kafkaTemplate.send("payment-completed", new PaymentCompletedEvent(
                    event.paymentId(), null, event.userId(), null, false, BigDecimal.ZERO));
        }
    }

    @KafkaListener(topics = "user-balance-update-responses")
    @Transactional
    public void handleUserBalanceUpdateResponse(UserBalanceUpdateResponseEvent event) {
        try {
            Payment payment = paymentRepository.findById(event.paymentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND, null, Level.WARN));
            completePayment(payment, event.success());
        } catch (Exception e) {
            log.error("Error handling user balance update response", e);
            kafkaTemplate.send("payment-completed", new PaymentCompletedEvent(
                    event.paymentId(), null, null, null, false, BigDecimal.ZERO));
        }
    }


    private void completePayment(Payment payment, boolean success) {
        payment.setStatus(success ? PaymentStatus.COMPLETE : PaymentStatus.FAILED);
        payment = paymentRepository.save(payment);

        kafkaTemplate.send("payment-completed", new PaymentCompletedEvent(
                payment.getPaymentId(),
                payment.getReservationId(),
                payment.getUserId(),
                payment.getPrice(),
                success,
                success ? payment.getPrice() : BigDecimal.ZERO
        ));

    }

    @KafkaListener(topics = "reservations-created")
    @Transactional
    public void handleReservationCreated(ReservationCreatedEvent event) {
        try {
            Payment payment = Payment.builder()
                    .reservationId(event.reservationId())
                    .userId(event.userId())
                    .status(PaymentStatus.READY)
                    .price(event.price())
                    .build();
            paymentRepository.save(payment);
            kafkaTemplate.send("user-balance-check-requests", new UserBalanceCheckRequestEvent(
                    payment.getUserId(), payment.getPaymentId()));
        } catch (Exception e) {
            log.error("Error handling reservation created", e);
            kafkaTemplate.send("payment-completed", new PaymentCompletedEvent(
                    null, event.reservationId(), event.userId(), event.price(), false, BigDecimal.ZERO));
        }
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
//        if (ReservationStatus.READY.equals(payment.getStatus())) {
//            // 결제 대기 상태 - 즉시 취소
//            updatedPayment = payment.updateStatus(ReservationStatus.CANCEL);
//        } else if (ReservationStatus.COMPLETE.equals(payment.getStatus())) {
//            // 결제 완료 상태 - 환불
//            updatedPayment = payment.updateStatus(ReservationStatus.REFUND);
//            user.refundBalance(payment.getPrice());
//        }
//
//        return updatedPayment;
//    }
}

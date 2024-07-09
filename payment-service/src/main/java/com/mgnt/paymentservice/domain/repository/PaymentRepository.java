package com.mgnt.paymentservice.domain.repository;

import com.mgnt.paymentservice.domain.entity.Payment;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository {

    Payment save(Payment payment);

    Payment findByReservation(Long reservationId);

    Optional<Payment> findByPaymentId(Long paymentId);

}

package com.mgnt.paymentservice.domain.repository;

import com.mgnt.paymentservice.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Payment save(Payment payment);

    Optional<Payment> findByReservationId(Long reservationId);

    Optional<Payment> findByPaymentId(Long paymentId);

//    Payment findByReservationId(Long reservationId);
}

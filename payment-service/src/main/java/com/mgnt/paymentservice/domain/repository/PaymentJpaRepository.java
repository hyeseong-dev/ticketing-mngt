package com.mgnt.paymentservice.domain.repository;

import com.mgnt.paymentservice.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    Payment findByReservationId(Long reservationId);
}

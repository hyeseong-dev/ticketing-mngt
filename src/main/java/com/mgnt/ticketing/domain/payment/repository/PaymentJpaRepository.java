package com.mgnt.ticketing.domain.payment.repository;

import com.mgnt.ticketing.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
}

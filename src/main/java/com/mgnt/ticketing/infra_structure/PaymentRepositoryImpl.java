package com.mgnt.ticketing.infra_structure;

import com.mgnt.ticketing.domain.payment.repository.PaymentJpaRepository;
import com.mgnt.ticketing.domain.payment.repository.PaymentRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private PaymentJpaRepository paymentJpaRepository;
}

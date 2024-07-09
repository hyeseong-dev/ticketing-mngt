package com.mgnt.ticketing.infra_structure.payment;

import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.payment.repository.PaymentJpaRepository;
import com.mgnt.ticketing.domain.payment.repository.PaymentRepository;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepositoryImpl {

    private final PaymentRepository paymentRepository;

    public PaymentRepositoryImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public Payment findByReservation(Reservation reservation) {
        return paymentRepository.findByReservation(reservation);
    }

    @Override
    public Payment findById(Long paymentId) {
        return paymentRepository.findById(paymentId).orElseThrow(EntityNotFoundException::new);
    }

}

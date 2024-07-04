package com.mgnt.ticketing.domain.payment.repository;

import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository {

    Payment save(Payment payment);

    Payment findByReservation(Reservation reservation);

    Payment findById(Long paymentId);
    
}

package com.mgnt.ticketing.domain.payment.repository;

import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    Payment findByReservation(Reservation reservation);
}

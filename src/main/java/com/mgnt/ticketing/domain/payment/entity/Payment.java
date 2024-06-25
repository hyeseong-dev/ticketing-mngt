package com.mgnt.ticketing.domain.payment.entity;

import com.mgnt.ticketing.base.entity.BaseDateTimeEntity;
import com.mgnt.ticketing.domain.payment.PaymentEnums;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "payment")
public class Payment extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Column(nullable = false)
    private PaymentEnums.Status status;

    private ZonedDateTime paidAt;

    public Payment(Reservation reservation, PaymentEnums.Status status, ZonedDateTime paidAt) {
        this.reservation = reservation;
        this.status = status;
        this.paidAt = paidAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment that = (Payment) o;
        return Objects.equals(paymentId, that.paymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId);
    }
}

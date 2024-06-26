package com.mgnt.ticketing.domain.reservation.entity;

import com.mgnt.ticketing.base.entity.BaseDateTimeEntity;
import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.payment.PaymentEnums;
import com.mgnt.ticketing.domain.payment.service.dto.CreatePaymentReqDto;
import com.mgnt.ticketing.domain.reservation.ReservationEnums;
import com.mgnt.ticketing.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "reservation")
public class Reservation extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")  // 수정: id -> user_id
    private User user;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "concert_id")
    private Concert concert;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "concert_date_id")
    private ConcertDate concertDate;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @Column(nullable = false)
    private ReservationEnums.Status status;

    private ZonedDateTime reservedAt;

    @Builder
    public Reservation(User user, Concert concert, ConcertDate concertDate, Seat seat, ReservationEnums.Status status, ZonedDateTime reservedAt) {
        this.user = user;
        this.concert = concert;
        this.concertDate = concertDate;
        this.seat = seat;
        this.status = status;
        this.reservedAt = reservedAt;
    }

    public CreatePaymentReqDto toCreatePayment() {
        return new CreatePaymentReqDto(this, PaymentEnums.Status.READY, this.seat.getPrice());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(reservationId, that.reservationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId);
    }
}

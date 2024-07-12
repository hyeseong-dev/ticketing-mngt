package com.mgnt.reservationservice.domain.entity;

import com.mgnt.core.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;


@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
public class Reservation extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "concert_id", nullable = false)
    private Long concertId;

    @Column(name = "concert_date_id", nullable = false)
    private Long concertDateId;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "reserved_at", nullable = false)
    private ZonedDateTime reservedAt;

    public void toComplete() {
        this.status = ReservationStatus.RESERVED;
    }

    @Builder
    public Reservation(Long userId, Long concertId, Long concertDateId, Long seatId, ReservationStatus status, ZonedDateTime reservedAt, BigDecimal price) {
        this.userId = userId;
        this.concertId = concertId;
        this.concertDateId = concertDateId;
        this.seatId = seatId;
        this.status = status;
        this.reservedAt = reservedAt;
        this.price = price;
    }

    public void updateStatus(ReservationStatus status) {
        this.status = status;
    }

    public void updatePrice(BigDecimal price) {
        this.price = price;
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

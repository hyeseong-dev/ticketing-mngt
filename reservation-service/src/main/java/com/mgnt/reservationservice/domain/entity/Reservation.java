package com.mgnt.reservationservice.domain.entity;

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
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
public class Reservation extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long concertId;

    @Column(nullable = false)
    private Long concertDateId;

    @Column(nullable = false)
    private int seatNum;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private BigDecimal price;

    private ZonedDateTime reservedAt;


    public enum Status {
        ING,
        RESERVED,
        CANCEL
    }

    public void toComplete() {
        this.status = Status.RESERVED;
    }

    @Builder
    public Reservation(Long userId, Long concertId, Long concertDateId, int seatNum,
                       Status status, ZonedDateTime reservedAt, BigDecimal price) {
        this.userId = userId;
        this.concertId = concertId;
        this.concertDateId = concertDateId;
        this.seatNum = seatNum;
        this.status = status;
        this.reservedAt = reservedAt;
        this.price = price;
    }

    public void updateStatus(Status status) {
        this.status = status;
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

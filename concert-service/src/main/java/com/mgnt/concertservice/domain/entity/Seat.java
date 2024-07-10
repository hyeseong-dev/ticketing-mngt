package com.mgnt.concertservice.domain.entity;

import com.mgnt.core.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "seat")
public class Seat extends BaseDateTimeEntity {

    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long seatId;


    @Column(name = "concert_date_id", nullable = false)
    private Long concertDateId;

    @Column(nullable = false)
    private int seatNum;

    @Column(nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Builder
    public Seat(Long seatId, Long concertDateId, int seatNum, BigDecimal price, SeatStatus status) {
        this.seatId = seatId;
        this.concertDateId = concertDateId;
        this.seatNum = seatNum;
        this.price = price;
        this.status = status;
    }
    
    public void patchStatus(SeatStatus status) {
        this.status = status;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seat seat = (Seat) o;
        return Objects.equals(seatId, seat.seatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seatId);
    }
}

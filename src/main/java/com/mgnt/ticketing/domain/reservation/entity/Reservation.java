package com.mgnt.ticketing.domain.reservation.entity;

import com.mgnt.ticketing.base.entity.BaseDateTimeEntity;
import com.mgnt.ticketing.domain.payment.entity.Payment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * 예약 엔티티 클래스
 * <p>
 * 이 클래스는 예약 정보를 나타내며, 데이터베이스의 'reservation' 테이블과 매핑됩니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "reservation")
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
    private Reservation.Status status;

    private ZonedDateTime reservedAt;


    public enum Status {
        ING,
        RESERVED,
        CANCEL
    }

    public void toComplete() {
        this.status = Status.RESERVED;
    }

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.REMOVE)
    private Payment payment;

    @Builder
    public Reservation(Long userId, Long concertId, Long concertDateId, int seatNum, Status status, ZonedDateTime reservedAt) {
        this.userId = userId;
        this.concertId = concertId;
        this.concertDateId = concertDateId;
        this.seatNum = seatNum;
        this.status = status;
        this.reservedAt = reservedAt;
    }

    /**
     * 객체 동등성 비교
     *
     * @param o 비교할 객체
     * @return 객체가 같으면 true, 그렇지 않으면 false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(reservationId, that.reservationId);
    }

    /**
     * 객체 해시 코드 반환
     *
     * @return 해시 코드
     */
    @Override
    public int hashCode() {
        return Objects.hash(reservationId);
    }
}

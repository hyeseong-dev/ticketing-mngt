package com.mgnt.ticketing.domain.payment.entity;

import com.mgnt.ticketing.base.entity.BaseDateTimeEntity;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * 결제 엔티티 클래스
 *
 * 이 클래스는 결제 정보를 나타내며, 데이터베이스의 'payment' 테이블과 매핑됩니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "payment")
public class Payment extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne()
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Payment.Status status;

    @Column(nullable = false)
    private BigDecimal price;

    private ZonedDateTime paidAt;

    public enum Status {
        READY,
        COMPLETE,
        CANCEL,
        REFUND
    }

    /**
     * 생성자
     *
     * @param reservation 예약 정보
     * @param status 결제 상태
     * @param price 결제 금액
     */
    @Builder
    public Payment(Reservation reservation, Payment.Status status, BigDecimal price) {
        this.reservation = reservation;
        this.status = status;
        this.price = price;
    }

    /**
     * 결제 상태 업데이트
     *
     * @param status 결제 상태
     * @return 업데이트된 Payment 객체
     */
    public Payment updateStatus(Payment.Status status) {
        if (status == null) {
            return null;
        }

        this.status = status;
        return this;
    }

    public Payment toPaid() {
        this.status = Payment.Status.COMPLETE;
        this.paidAt = ZonedDateTime.now();
        return this;
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
        Payment that = (Payment) o;
        return Objects.equals(paymentId, that.paymentId);
    }

    /**
     * 객체 해시 코드 반환
     *
     * @return 해시 코드
     */
    @Override
    public int hashCode() {
        return Objects.hash(paymentId);
    }
}

package com.mgnt.paymentservice.domain.entity;

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
 * <p>
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

    @Column(nullable = false)
    private Long userId;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private BigDecimal price;

    private ZonedDateTime paidAt;

    public enum Status {
        READY,
        COMPLETE,
        CANCEL,
        FAILED,
        REFUND
    }

    @Builder
    public Payment(Long paymentId, Long userId, Long reservationId, Status status, BigDecimal price) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.reservationId = reservationId;
        this.status = status;
        this.price = price;
    }


    public void setStatus(Status status) {
        this.status = status;
        if (status == Status.COMPLETE) {
            this.paidAt = ZonedDateTime.now();
        }
    }

    public Payment toPaid() {
        this.status = Status.COMPLETE;
        this.paidAt = ZonedDateTime.now();
        return this;
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

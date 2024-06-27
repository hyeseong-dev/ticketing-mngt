package com.mgnt.ticketing.domain.concert.entity;

import com.mgnt.ticketing.base.entity.BaseDateTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 좌석 엔티티 클래스
 *
 * 이 클래스는 좌석 정보를 나타내며, 데이터베이스의 'seat' 테이블과 매핑됩니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "seat")
public class Seat extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long seatId;

    @Column(nullable = false)
    private int seatNum;

    @Column(nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    /**
     * 생성자
     *
     * @param seatId 좌석 ID
     * @param seatNum 좌석 번호
     * @param price 좌석 가격
     */
    public Seat(Long seatId, int seatNum, BigDecimal price) {
        this.seatId = seatId;
        this.seatNum = seatNum;
        this.price = price;
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
        Seat seat = (Seat) o;
        return Objects.equals(seatId, seat.seatId);
    }

    /**
     * 객체 해시 코드 반환
     *
     * @return 해시 코드
     */
    @Override
    public int hashCode() {
        return Objects.hash(seatId);
    }
}

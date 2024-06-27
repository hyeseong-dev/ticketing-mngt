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

/**
 * 예약 엔티티 클래스
 *
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

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
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

    /**
     * 생성자
     *
     * @param user 사용자
     * @param concert 콘서트
     * @param concertDate 콘서트 날짜
     * @param seat 좌석
     * @param status 예약 상태
     * @param reservedAt 예약 시간
     */
    @Builder
    public Reservation(User user, Concert concert, ConcertDate concertDate, Seat seat, ReservationEnums.Status status, ZonedDateTime reservedAt) {
        this.user = user;
        this.concert = concert;
        this.concertDate = concertDate;
        this.seat = seat;
        this.status = status;
        this.reservedAt = reservedAt;
    }

    /**
     * 결제 생성 요청 DTO로 변환
     *
     * @return 결제 생성 요청 DTO
     */
    public CreatePaymentReqDto toCreatePayment() {
        return new CreatePaymentReqDto(this, PaymentEnums.Status.READY, this.seat.getPrice());
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

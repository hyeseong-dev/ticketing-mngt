package com.mgnt.concertservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 콘서트 날짜 엔티티 클래스
 * <p>
 * 이 클래스는 콘서트 날짜 정보를 나타내며, 데이터베이스의 'concert_date' 테이블과 매핑됩니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "concert_date")
public class ConcertDate extends BaseDateTimeEntity {

    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long concertDateId;

    @Column(nullable = false)
    private ZonedDateTime concertDate;

    @Column(name = "concert_id", nullable = false)
    private Concert concert;

    @OneToMany(mappedBy = "concertDate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seat> seats = new ArrayList<>();

    @Builder
    public ConcertDate(Long concertDateId, Concert concert, ZonedDateTime concertDate) {
        this.concertDateId = concertDateId;
        this.concert = concert;
        this.concertDate = concertDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConcertDate that = (ConcertDate) o;
        return Objects.equals(concertDateId, that.concertDateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(concertDateId);
    }
}

package com.mgnt.ticketing.domain.concert.entity;

import com.mgnt.ticketing.base.entity.BaseDateTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 콘서트 날짜 엔티티 클래스
 *
 * 이 클래스는 콘서트 날짜 정보를 나타내며, 데이터베이스의 'concert_date' 테이블과 매핑됩니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "concert_date")
public class ConcertDate extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long concertDateId;

    @Column(nullable = false)
    private ZonedDateTime concertDate;

    /**
     * 생성자
     *
     * @param concertDateId 콘서트 날짜 ID
     * @param concertDate 콘서트 날짜
     */
    @Builder
    public ConcertDate(Long concertDateId, ZonedDateTime concertDate) {
        this.concertDateId = concertDateId;
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

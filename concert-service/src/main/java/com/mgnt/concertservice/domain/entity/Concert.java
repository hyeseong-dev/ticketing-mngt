package com.mgnt.concertservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 콘서트 엔티티 클래스
 * <p>
 * 이 클래스는 콘서트 정보를 나타내며, 데이터베이스의 'concert' 테이블과 매핑됩니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "concert")
public class Concert extends BaseDateTimeEntity {

    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long concertId;

    @Column(nullable = false, length = 50)
    private String name;

    @JoinColumn(name = "place_id")
    private Long placeId;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "concert_date_id")
    private List<ConcertDate> concertDateList = new ArrayList<>();


    @Builder
    public Concert(String name, Long placeId, List<ConcertDate> concertDateList) {
        this.name = name;
        this.placeId = placeId;
        this.concertDateList = concertDateList == null ? new ArrayList<>() : concertDateList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Concert concert = (Concert) o;
        return Objects.equals(concertId, concert.concertId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(concertId);
    }
}

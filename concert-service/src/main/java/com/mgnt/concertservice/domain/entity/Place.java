package com.mgnt.concertservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 공연 장소 엔티티 클래스
 * <p>
 * 이 클래스는 공연 장소 정보를 나타내며, 데이터베이스의 'place' 테이블과 매핑됩니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "place")
public class Place extends BaseDateTimeEntity {

    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long placeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int seatsCnt = 0;

    /**
     * 생성자
     *
     * @param name     장소 이름
     * @param seatsCnt 좌석 수
     */
    @Builder
    public Place(String name, int seatsCnt) {
        this.name = name;
        this.seatsCnt = seatsCnt;
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
        Place place = (Place) o;
        return Objects.equals(placeId, place.placeId);
    }

    /**
     * 객체 해시 코드 반환
     *
     * @return 해시 코드
     */
    @Override
    public int hashCode() {
        return Objects.hash(placeId);
    }
}

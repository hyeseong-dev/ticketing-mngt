package com.mgnt.temp.domain.entity;

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

    @OneToOne
    @JoinColumn(name = "place_id")
    private Place place;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "concert_id")
    private List<ConcertDate> concertDateList = new ArrayList<>();

    /**
     * 생성자
     *
     * @param name            콘서트 이름
     * @param place           장소 객체
     * @param concertDateList 콘서트 날짜 목록
     */
    @Builder
    public Concert(String name, Place place, List<ConcertDate> concertDateList) {
        this.name = name;
        this.place = place;
        this.concertDateList = concertDateList;
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
        Concert concert = (Concert) o;
        return Objects.equals(concertId, concert.concertId);
    }

    /**
     * 객체 해시 코드 반환
     *
     * @return 해시 코드
     */
    @Override
    public int hashCode() {
        return Objects.hash(concertId);
    }
}

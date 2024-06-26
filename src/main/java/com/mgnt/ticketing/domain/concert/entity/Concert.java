package com.mgnt.ticketing.domain.concert.entity;


import com.mgnt.ticketing.base.entity.BaseDateTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "concert")
public class Concert extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long concertId;

    @Column(nullable = false, length = 50)
    private String name;

    private Long placeId;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "concert_id")
    private List<ConcertDate> concertDateList = new ArrayList();

    @Builder
    public Concert(Long concertId, String name, Long placeId, List<ConcertDate> concertDateList) {
        this.concertId = concertId;
        this.name = name;
        this.placeId = placeId;
        this.concertDateList = concertDateList != null ? concertDateList : new ArrayList<>();
    }

    // setConcertDateList 메서드 추가
    public void setConcertDateList(List<ConcertDate> concertDateList) {
        this.concertDateList = concertDateList;
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

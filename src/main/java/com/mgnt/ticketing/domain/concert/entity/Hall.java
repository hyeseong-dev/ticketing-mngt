package com.mgnt.ticketing.domain.concert.entity;

import com.mgnt.ticketing.base.entity.BaseDateTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
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
@Table(name = "hall")
public class Hall extends BaseDateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hallId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int seats_cnt = 0;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "seat_id")
    private List<Seat> seatList = new ArrayList();

    public Hall(String name, int seats_cnt) {
        this.name = name;
        this.seats_cnt = seats_cnt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hall hall = (Hall) o;
        return Objects.equals(hallId, hall.hallId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hallId);
    }
}

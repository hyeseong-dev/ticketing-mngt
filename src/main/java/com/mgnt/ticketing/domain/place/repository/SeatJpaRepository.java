package com.mgnt.ticketing.domain.place.repository;

import com.mgnt.ticketing.domain.place.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatJpaRepository extends JpaRepository<Seat, Long> {
}

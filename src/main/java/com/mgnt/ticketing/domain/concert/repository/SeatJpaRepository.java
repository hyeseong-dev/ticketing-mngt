package com.mgnt.ticketing.domain.concert.repository;

import com.mgnt.ticketing.domain.concert.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatJpaRepository extends JpaRepository<Seat, Long> {
}

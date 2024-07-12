package com.mgnt.concertservice.domain.repository;

import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.core.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long>, SeatRepositoryCustom {

    boolean existsByConcertDateIdAndStatus(Long concertDateId, SeatStatus status);

    List<Seat> findAllByConcertDateIdAndStatus(Long concertDateId, SeatStatus status);

    List<Seat> findAllByConcertDateId(Long concertDateId);
}

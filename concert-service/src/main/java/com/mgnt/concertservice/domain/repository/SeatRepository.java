package com.mgnt.concertservice.domain.repository;

import com.mgnt.concertservice.domain.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    boolean existsByConcertDateIdAndStatus(Long concertDateId, Seat.Status status);

    List<Seat> findAllByConcertDateIdAndStatus(Long concertDateId, Seat.Status status);


    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select s from Seat s where s.concertDateId = :concertDateId and s.seatId = :seatId")
    Seat findSeatByConcertDate_concertDateIdAndSeatId(@Param("concertDateId") Long concertDateId, @Param("seatId") Long seatId);

    @Modifying
    @Query("UPDATE Seat s SET s.status = :status WHERE s.concertDateId = :concertDateId AND s.seatId = :seatId AND s.status = :currentStatus")
    @Transactional
    int updateSeatStatus(@Param("concertDateId") Long concertDateId, @Param("seatId") Long seatId, @Param("status") Seat.Status status, @Param("currentStatus") Seat.Status currentStatus);

    default boolean checkAndUpdateSeatStatus(Long concertDateId, Long seatId, Seat.Status status) {
        return updateSeatStatus(concertDateId, seatId, status, Seat.Status.AVAILABLE) > 0;
    }
}



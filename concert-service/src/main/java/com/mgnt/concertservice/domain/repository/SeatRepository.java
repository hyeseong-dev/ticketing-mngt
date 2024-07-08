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

    boolean existsByConcertDate_concertDateIdAndStatus(Long concertDateId, Seat.Status status);

    List<Seat> findAllByConcertDate_concertDateIdAndStatus(Long concertDateId, Seat.Status status);


    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select s from Seat s where s.concertDate.concertDateId = :concertDateId and s.seatNum = :seatNum")
    Seat findSeatByConcertDate_concertDateIdAndSeatNum(@Param("concertDateId") Long concertDateId, @Param("seatNum") int seatNum);

    @Modifying
    @Query("UPDATE Seat s SET s.status = :status WHERE s.concertDate.concertDateId = :concertDateId AND s.seatNum = :seatNum AND s.status = com.mgnt.concertservice.domain.entity.Seat.Status.AVAILABLE")
    @Transactional
    int updateSeatStatus(@Param("concertDateId") Long concertDateId, @Param("seatNum") int seatNum, @Param("status") Seat.Status status);

    default boolean checkAndUpdateSeatStatus(Long concertDateId, int seatNum, Seat.Status status) {
        return updateSeatStatus(concertDateId, seatNum, status) > 0;
    }
}



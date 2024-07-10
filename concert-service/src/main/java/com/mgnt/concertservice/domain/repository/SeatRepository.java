package com.mgnt.concertservice.domain.repository;

import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.core.enums.SeatStatus;
import com.mgnt.core.event.ReservationConfirmedEvent;
import io.lettuce.core.ScanIterator;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long>, SeatRepositoryCustom {

    boolean existsByConcertDateIdAndStatus(Long concertDateId, SeatStatus status);

    List<Seat> findAllByConcertDateIdAndStatus(Long concertDateId, SeatStatus status);


    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT s FROM Seat s WHERE s.concertDateId = :concertDateId AND s.seatId = :seatId")
    Optional<Seat> findSeatByConcertDate_concertDateIdAndSeatId(
            @Param("concertDateId") Long concertDateId,
            @Param("seatId") Long seatId);

    List<Seat> findAllByConcertDateId(Long concertDateId);

//    @Modifying
//    @Query("UPDATE Seat s SET s.status = :status WHERE s.concertDateId = :concertDateId AND s.seatId = :seatId")
//    int updateSeatStatus(
//            @Param("concertDateId") Long concertDateId,
//            @Param("seatId") Long seatId,
//            @Param("status") SeatStatus status
//    );

}



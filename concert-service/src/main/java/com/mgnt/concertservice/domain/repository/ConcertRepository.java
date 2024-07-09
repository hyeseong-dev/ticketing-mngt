package com.mgnt.concertservice.domain.repository;

import com.mgnt.concertservice.domain.entity.Concert;
import com.mgnt.concertservice.domain.entity.ConcertDate;
import com.mgnt.concertservice.domain.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConcertRepository extends JpaRepository<Concert, Long> {

    @Override
    List<Concert> findAll();

    Optional<Concert> findByConcertId(Long concertId);

    @Query("SELECT cd FROM ConcertDate cd WHERE cd.concertDateId = :concertDateId")
    Optional<ConcertDate> findConcertDateById(@Param("concertDateId") Long concertDateId);

    @Query("SELECT s FROM Seat s WHERE s.concertDateId = :concertDateId AND s.status = :status")
    List<Seat> findSeatsByConcertDateIdAndStatus(@Param("concertDateId") Long concertDateId, @Param("status") Seat.Status status);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Seat s WHERE s.concertDateId = :concertDateId AND s.status = :status")
    boolean existsByConcertDateAndStatus(@Param("concertDateId") Long concertDateId, @Param("status") Seat.Status status);

    @Query("SELECT s FROM Seat s WHERE s.concertDateId = :concertDateId AND s.seatNum = :seatId")
    Optional<Seat> findSeatByConcertDateIdAndSeatNum(@Param("concertDateId") Long concertDateId, @Param("seatId") Long seatId);
}

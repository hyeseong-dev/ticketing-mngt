//package com.mgnt.reservationservice.infra_structure;
//
//import com.mgnt.reservationservice.controller.dto.response.ReserveResponse;
//import com.mgnt.reservationservice.domain.entity.Reservation;
//import com.mgnt.reservationservice.domain.repository.ReservationJpaRepository;
//import com.mgnt.reservationservice.domain.repository.ReservationRepository;
//import jakarta.persistence.EntityNotFoundException;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public class ReservationRepositoryImpl implements ReservationRepository {
//
//    private final ReservationJpaRepository reservationJpaRepository;
//
//    public ReservationRepositoryImpl(ReservationJpaRepository reservationJpaRepository) {
//        this.reservationJpaRepository = reservationJpaRepository;
//    }
//
//    @Override
//    public List<ReserveResponse> getMyReservations(Long userId) {
//        return reservationJpaRepository.getMyReservations(userId);
//    }
//
//    public Reservation findOneByConcertDateIdAndSeatNum(Long concertDateId, int seatNum) {
//        return reservationJpaRepository.findOneByConcertDateIdAndSeatNum(concertDateId, seatNum);
//    }
//
//    @Override
//    public Reservation save(Reservation reservation) {
//        return reservationJpaRepository.save(reservation);
//    }
//
//    @Override
//    public Reservation findByIdAndUserId(Long reservationId, Long userId) {
//        return reservationJpaRepository.findByReservationIdAndUserId(reservationId, userId);
//    }
//
//    @Override
//    public void delete(Reservation reservation) {
//        reservationJpaRepository.delete(reservation);
//    }
//
//    @Override
//    public Reservation findById(Long reservationId) {
//        return reservationJpaRepository.findById(reservationId).orElseThrow(EntityNotFoundException::new);
//    }
//
//}

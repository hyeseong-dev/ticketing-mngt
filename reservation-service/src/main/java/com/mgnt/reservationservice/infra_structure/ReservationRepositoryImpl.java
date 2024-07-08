package com.mgnt.reservationservice.infra_structure;

import com.mgnt.reservationservice.controller.dto.response.ReserveResponse;
import com.mgnt.reservationservice.domain.entity.Reservation;
import com.mgnt.reservationservice.domain.repository.ReservationJpaRepository;
import com.mgnt.reservationservice.domain.repository.ReservationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;

    public ReservationRepositoryImpl(ReservationJpaRepository reservationJpaRepository) {
        this.reservationJpaRepository = reservationJpaRepository;
    }

    @Override
    public List<ReserveResponse> getMyReservations(Long userId) {
        return reservationJpaRepository.getMyReservations(userId);
    }

    @Override
    public List<Reservation> findAllByConcertDateId(Long concertDateId) {
        return List.of();
    }

    @Override
    public Reservation findByReservationIdAndUserId(Long reservationId, Long userId) {
        return null;
    }

    public Reservation findOneByConcertDateIdAndSeatNum(Long concertDateId, int seatNum) {
        return reservationJpaRepository.findOneByConcertDateIdAndSeatNum(concertDateId, seatNum);
    }

    @Override
    public Reservation save(Reservation reservation) {
        return reservationJpaRepository.save(reservation);
    }

    @Override
    public Reservation findByIdAndUserId(Long reservationId, Long userId) {
        return reservationJpaRepository.findByReservationIdAndUserId(reservationId, userId);
    }

    @Override
    public void delete(Reservation reservation) {
        reservationJpaRepository.delete(reservation);
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends Reservation> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public Reservation findByReservationId(Long reservationId) {
        return reservationJpaRepository.findById(reservationId).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends Reservation> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends Reservation> List<S> saveAllAndFlush(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public void deleteAllInBatch(Iterable<Reservation> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public Reservation getOne(Long aLong) {
        return null;
    }

    @Override
    public Reservation getById(Long aLong) {
        return null;
    }

    @Override
    public Reservation getReferenceById(Long aLong) {
        return null;
    }

    @Override
    public <S extends Reservation> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Reservation> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends Reservation> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends Reservation> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Reservation> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Reservation> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends Reservation, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends Reservation> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public Optional<Reservation> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public List<Reservation> findAll() {
        return List.of();
    }

    @Override
    public List<Reservation> findAllById(Iterable<Long> longs) {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public List<Reservation> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<Reservation> findAll(Pageable pageable) {
        return null;
    }
}

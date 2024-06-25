package com.mgnt.ticketing.infra_structure;

import com.mgnt.ticketing.domain.reservation.repository.ReservationJpaRepository;
import com.mgnt.ticketing.domain.reservation.repository.ReservationRepository;

public class ReservationRepositoryImpl implements ReservationRepository {

    private ReservationJpaRepository reservationJpaRepository;
}

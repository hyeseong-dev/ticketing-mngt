package com.mgnt.ticketing.domain.reservation.event;

import com.mgnt.ticketing.domain.reservation.service.ReservationMonitor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReservationEventListener {

    private final ReservationMonitor reservationMonitor;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onReservationOccupiedEvent(ReservationOccupiedEvent event) {
        reservationMonitor.occupyReservation(event.getReservationId());
    }
}

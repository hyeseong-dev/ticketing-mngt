package com.mgnt.reservationservice.domain.event;

import com.mgnt.ticketing.domain.reservation.service.ReservationMonitor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReservationEventListener {

    private final ReservationMonitor reservationMonitor;

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservationOccupiedEvent(ReservationOccupiedEvent event) {
        reservationMonitor.occupyReservation(event.getReservationId());
    }
}

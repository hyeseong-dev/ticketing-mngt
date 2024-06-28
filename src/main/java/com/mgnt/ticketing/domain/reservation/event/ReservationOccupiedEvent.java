package com.mgnt.ticketing.domain.reservation.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReservationOccupiedEvent extends ApplicationEvent {

    private final Long reservationId;

    public ReservationOccupiedEvent(Object source, Long reservationId) {
        super(source);
        this.reservationId = reservationId;
    }
}
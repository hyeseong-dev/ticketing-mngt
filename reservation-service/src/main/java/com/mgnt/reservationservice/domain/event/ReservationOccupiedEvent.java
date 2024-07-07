package com.mgnt.reservationservice.domain.event;

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
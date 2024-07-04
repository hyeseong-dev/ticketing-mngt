package com.mgnt.ticketing.domain;

import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class TimeService {

    private Clock clock = Clock.systemDefaultZone();

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public Clock getClock() {
        return clock;
    }
}

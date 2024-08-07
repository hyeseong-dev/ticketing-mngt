package com.mgnt.concertservice.domain.service;

import com.mgnt.concertservice.controller.response.GetConcertResponse;
import com.mgnt.concertservice.controller.response.GetConcertsResponse;
import com.mgnt.concertservice.controller.response.GetDatesResponse;
import com.mgnt.concertservice.controller.response.GetSeatsResponse;
import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.core.enums.SeatStatus;
import com.mgnt.core.event.concert_service.ConcertInfoRequestEvent;
import com.mgnt.core.event.concert_service.SeatStatusUpdatedEvent;
import com.mgnt.core.event.reservation_service.ReservationConfirmedEvent;
import com.mgnt.core.event.reservation_service.ReservationRequestedEvent;

import java.util.List;

public interface ConcertService {

    // 기존 메서드들
    List<GetConcertsResponse> getConcerts();

    GetConcertResponse getConcert(Long concertId);

    GetDatesResponse getDates(Long concertId);

    List<Seat> getAvailableSeats(Long concertDateId);

    void patchSeatStatus(Long concertDateId, Long seatId, SeatStatus status);
}
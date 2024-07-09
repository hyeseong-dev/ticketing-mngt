package com.mgnt.concertservice.domain.service;

import com.mgnt.concertservice.controller.response.GetConcertResponse;
//import com.mgnt.concertservice.controller.response.GetConcertsResponse;
import com.mgnt.concertservice.controller.response.GetDatesResponse;
import com.mgnt.concertservice.controller.response.GetSeatsResponse;
import com.mgnt.concertservice.domain.entity.Concert;
import com.mgnt.concertservice.domain.entity.ConcertDate;
import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.concertservice.domain.repository.ConcertRepository;
import com.mgnt.concertservice.domain.repository.SeatRepository;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.event.*;
import com.mgnt.core.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class ConcertService implements ConcertInterface {

    private final ConcertRepository concertRepository;
    private final ConcertValidator concertValidator;
    private final TransactionTemplate transactionTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SeatRepository seatRepository;

    @KafkaListener(topics = "reservation-requests")
    public void handleReservationRequest(ReservationRequestedEvent event) {
        Seat seat = seatRepository.findSeatByConcertDate_concertDateIdAndSeatId(event.concertDateId(), event.seatId());
        concertValidator.checkSeatAvailability(seat);

        boolean isAvailable = seatRepository.checkAndUpdateSeatStatus(
                event.concertDateId(), event.seatId(), Seat.Status.DISABLE);

        SeatStatusUpdatedEvent updateEvent = new SeatStatusUpdatedEvent(
                null, // 이 시점까지 예약처리 요청이 마무리 된 것이 아니므로
                event.userId(),
                event.concertId(),
                event.concertDateId(),
                event.seatId(),
                isAvailable
        );
        kafkaTemplate.send("seat-status-updates", updateEvent);
    }

    @KafkaListener(topics = "reservation-failed")
    public void handleReservationFailed(ReservationFailedEvent event) {
        seatRepository.checkAndUpdateSeatStatus(event.concertDateId(), event.seatId(), Seat.Status.AVAILABLE);
        log.info("Seat status reverted to AVAILABLE: concertDateId={}, seatId={}", event.concertDateId(), event.seatId());
    }

    @KafkaListener(topics = "concert-info-requests")
    public void handleConcertInfoRequest(ConcertInfoRequestEvent event) {
        Concert concert = concertRepository.findById(event.concertId()).orElseThrow();
        ConcertDate concertDate = concert.getConcertDateList().stream()
                .filter(cd -> cd.getConcertDateId().equals(event.concertDateId()))
                .findFirst().orElseThrow();
        Seat seat = seatRepository.findSeatByConcertDate_concertDateIdAndSeatId(event.concertDateId(), event.seatId());

        ConcertInfoResponseEvent responseEvent = new ConcertInfoResponseEvent(
                event.reservationId(),
                new ConcertInfoDTO(concert.getConcertId(), concert.getName()),
                new ConcertDateDTO(concertDate.getConcertDateId(), concertDate.getConcertDate()),
                new SeatDTO(seat.getSeatId(), seat.getSeatNum())
        );
        kafkaTemplate.send("concert-info-responses", responseEvent);
    }

//    @Override
//    public List<GetConcertsResponse> getConcerts() {
//        List<Concert> concerts = concertRepository.findAll();
//        return concerts.stream().map(GetConcertsResponse::from).toList();
//    }

//    @Override
//    public GetConcertResponse getConcert(Long concertId) {
//        Concert concert = concertRepository.findById(concertId)
//                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND, null, Level.WARN));
//        return GetConcertResponse.from(concert);
//    }

    public GetDatesResponse getDates(Long concertId) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND, null, Level.WARN));
        concertValidator.dateIsNull(concert.getConcertDateList());

        List<GetDatesResponse.DateInfo> dateInfos = new ArrayList<>();
        concert.getConcertDateList().forEach(concertDate -> {
            boolean available = seatRepository.existsByConcertDateIdAndStatus(concertDate.getConcertDateId(), Seat.Status.AVAILABLE);
            dateInfos.add(GetDatesResponse.DateInfo.from(concertDate, available));
        });

        return new GetDatesResponse(dateInfos);
    }

    public GetSeatsResponse getAvailableSeats(Long concertDateId) {
        List<Seat> availableSeats = concertRepository.findSeatsByConcertDateIdAndStatus(concertDateId, Seat.Status.AVAILABLE);
        return GetSeatsResponse.from(availableSeats);
    }

    public void patchSeatStatus(Long concertDateId, Long seatId, Seat.Status status) {
        Seat seat = concertRepository.findSeatByConcertDateIdAndSeatNum(concertDateId, seatId)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, null, Level.WARN));
        seat.patchStatus(status);
    }
}

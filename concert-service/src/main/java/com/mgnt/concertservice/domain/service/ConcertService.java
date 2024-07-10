package com.mgnt.concertservice.domain.service;

import com.mgnt.concertservice.controller.response.GetConcertResponse;
//import com.mgnt.concertservice.controller.response.GetConcertsResponse;
import com.mgnt.concertservice.controller.response.GetConcertsResponse;
import com.mgnt.concertservice.controller.response.GetDatesResponse;
import com.mgnt.concertservice.controller.response.GetSeatsResponse;
import com.mgnt.concertservice.domain.entity.Concert;
import com.mgnt.concertservice.domain.entity.ConcertDate;
import com.mgnt.concertservice.domain.entity.Place;
import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.concertservice.domain.repository.ConcertRepository;
import com.mgnt.concertservice.domain.repository.PlaceRepository;
import com.mgnt.concertservice.domain.repository.SeatRepository;
import com.mgnt.core.enums.SeatStatus;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.event.*;
import com.mgnt.core.exception.CustomException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.apache.logging.log4j.Level;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ConcertService implements ConcertInterface {

    private final ConcertRepository concertRepository;
    private final ConcertValidator concertValidator;
    private final PlaceRepository placeRepository;
    private final TransactionTemplate transactionTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SeatRepository seatRepository;

    @KafkaListener(topics = "reservation-requests")
    @Transactional
    public void handleReservationRequest(ReservationRequestedEvent event) {
        log.info("예약 요청 수신: {}", event);
        try {
            Seat seat = findAndValidateSeat(event);
            updateSeatStatus(seat, event);
            sendSeatStatusUpdate(seat, event);
        } catch (Exception e) {
            handleReservationError(e, event);
        }
    }

    private Seat findAndValidateSeat(ReservationRequestedEvent event) {
        Seat seat = seatRepository.findAvailableSeatByConcertDateIdAndSeatId(event.concertDateId(), event.seatId())
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, null, Level.WARN));
        return seat;
    }

    private void updateSeatStatus(Seat seat, ReservationRequestedEvent event) {
        int updatedRows = seatRepository.updateSeatStatus(event.concertDateId(), event.seatId(), SeatStatus.DISABLE);
        if (updatedRows == 0) {
            throw new OptimisticLockingFailureException("좌석이 동시에 업데이트되었습니다.");
        }
    }

    private void sendSeatStatusUpdate(Seat seat, ReservationRequestedEvent event) {
        SeatStatusUpdatedEvent updateEvent = new SeatStatusUpdatedEvent(
                null, event.userId(), event.concertId(), event.concertDateId(),
                event.seatId(), seat.getPrice()
        );
        kafkaTemplate.send("seat-status-updates", updateEvent)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("메시지 전송 성공: {}", updateEvent);
                    } else {
                        log.error("메시지 전송 실패: {}", ex.toString());
                    }
                });
    }

    private void handleReservationError(Exception e, ReservationRequestedEvent event) {
        if (e instanceof CustomException) {
            log.warn("예약 처리 중 예외 발생: {}", e.getMessage());
            sendReservationFailureNotification(event);
        } else if (e instanceof OptimisticLockingFailureException) {
            log.warn("동시 수정 감지: {}", e.getMessage());
            sendReservationFailureNotification(event);
        } else {
            log.error("예약 처리 중 예상치 못한 오류 발생", e);
            sendReservationFailureNotification(event);
        }

    }

    private void sendReservationFailureNotification(ReservationRequestedEvent event) {
        log.warn("예약 실패에 따른 알림 메시지 발신: {}", event.toString());
        // 예약 실패 알림 메시지 전송 로직 구현
        // 예를 들어, Kafka 메시지 발행 등의 방식으로 처리할 수 있음
    }

    @Transactional
    @KafkaListener(topics = "reservation-failed")
    public void handleReservationFailed(ReservationFailedEvent event) {
        try {
            Seat seat = seatRepository.findById(event.seatId())
                    .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, null, Level.WARN));
            seat.patchStatus(SeatStatus.AVAILABLE);
            seatRepository.save(seat);
        } catch (Exception e) {
            log.error("Error handling reservation failed", e);
            // 추후 관리자에게 알림 보내기 설정 로직 처리
        }
    }

    @Transactional
    @KafkaListener(topics = "concert-info-requests")
    public void handleConcertInfoRequest(ConcertInfoRequestEvent event) {
        Concert concert = concertRepository.findById(event.concertId())
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND, null, Level.INFO));
        ConcertDate concertDate = concert.getConcertDateList().stream()
                .filter(cd -> cd.getConcertDateId().equals(event.concertDateId()))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_DATE_NOT_FOUND, null, Level.INFO));
        Seat seat = seatRepository.findById(event.seatId())
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, null, Level.INFO));

        ConcertInfoResponseEvent responseEvent = new ConcertInfoResponseEvent(
                event.reservationId(),
                new ConcertInfoDTO(concert.getConcertId(), concert.getName()),
                new ConcertDateDTO(concertDate.getConcertDateId(), concertDate.getConcertDate()),
                new SeatDTO(seat.getSeatId(), seat.getSeatNum())
        );
        kafkaTemplate.send("concert-info-responses", responseEvent);
    }

    @Transactional
    @KafkaListener(topics = "reservation-confirmed")
    public void handleReservationConfirmed(ReservationConfirmedEvent event) {
        Seat seat = seatRepository.findSeatByConcertDate_concertDateIdAndSeatId(event.concertDateId(), event.seatId())
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, null, Level.WARN));

        seatRepository.updateSeatStatus(event.concertDateId(), event.seatId(), event.status());
    }

    @Override
    public List<GetConcertsResponse> getConcerts() {
        return concertRepository.findAll().stream()
                .map(concert -> {
                    String placeName = getPlaceName(concert.getPlaceId());
                    return GetConcertsResponse.of(concert, placeName);
                })
                .collect(Collectors.toList());
    }

    public GetConcertResponse getConcert(Long concertId) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new EntityNotFoundException("Concert not found"));
        String placeName = getPlaceName(concert.getPlaceId());
        String price = calculatePrice(concert);
        return GetConcertResponse.of(concert, placeName, price);
    }

    private String getPlaceName(Long placeId) {
        return placeRepository.findByPlaceId(placeId)
                .map(Place::getName)
                .orElse("-");
    }


    private String calculatePrice(Concert concert) {
        // 콘서트의 모든 날짜에 대한 좌석 가격의 범위를 계산합니다.
        List<BigDecimal> prices = concert.getConcertDateList().stream()
                .flatMap(date -> seatRepository.findAllByConcertDateId(date.getConcertDateId()).stream())
                .map(Seat::getPrice)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (prices.isEmpty()) {
            return "Price not set";
        } else if (prices.size() == 1) {
            return formatPrice(prices.get(0));
        } else {
            return formatPrice(prices.get(0)) + " - " + formatPrice(prices.get(prices.size() - 1));
        }
    }

    private String formatPrice(BigDecimal price) {
        return String.format("%,d원", price.setScale(0, RoundingMode.HALF_UP).intValue());
    }

    public GetDatesResponse getDates(Long concertId) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND, null, Level.WARN));
        concertValidator.dateIsNull(concert.getConcertDateList());

        List<GetDatesResponse.DateInfo> dateInfos = new ArrayList<>();
        concert.getConcertDateList().forEach(concertDate -> {
            boolean available = seatRepository.existsByConcertDateIdAndStatus(concertDate.getConcertDateId(), SeatStatus.AVAILABLE);
            dateInfos.add(GetDatesResponse.DateInfo.from(concertDate, available));
        });

        return new GetDatesResponse(dateInfos);
    }

    public GetSeatsResponse getAvailableSeats(Long concertDateId) {
        List<Seat> availableSeats = concertRepository.findSeatsByConcertDateIdAndStatus(concertDateId, SeatStatus.AVAILABLE);
        return GetSeatsResponse.from(availableSeats);
    }

    public void patchSeatStatus(Long concertDateId, Long seatId, SeatStatus status) {
        Seat seat = concertRepository.findSeatByConcertDateIdAndSeatId(concertDateId, seatId)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, null, Level.WARN));
        seat.patchStatus(status);
    }
}

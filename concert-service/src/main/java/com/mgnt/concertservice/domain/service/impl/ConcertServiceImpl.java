package com.mgnt.concertservice.domain.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgnt.concertservice.controller.response.GetConcertResponse;
import com.mgnt.concertservice.controller.response.GetConcertsResponse;
import com.mgnt.concertservice.controller.response.GetDatesResponse;
import com.mgnt.concertservice.controller.response.GetSeatsResponse;
import com.mgnt.concertservice.domain.entity.*;
import com.mgnt.concertservice.domain.repository.*;
import com.mgnt.concertservice.domain.service.ConcertInterface;
import com.mgnt.concertservice.domain.service.ConcertService;
import com.mgnt.concertservice.domain.service.ConcertValidator;
import com.mgnt.core.enums.SeatStatus;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.event.concert_service.*;
import com.mgnt.core.event.reservation_service.ReservationConfirmedEvent;
import com.mgnt.core.event.reservation_service.ReservationFailedEvent;
import com.mgnt.core.event.reservation_service.ReservationRequestedEvent;
import com.mgnt.core.exception.CustomException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ConcertServiceImpl implements ConcertService {

    private final ConcertRepository concertRepository;
    private final PlaceRepository placeRepository;
    private final SeatRepository seatRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ConcertValidator concertValidator;
    private final InventoryRepository inventoryRepository;


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

    public List<Seat> getAvailableSeats(Long concertDateId) {
        List<Seat> availableSeats = concertRepository.findSeatsByConcertDateIdAndStatus(concertDateId, SeatStatus.AVAILABLE);
        return availableSeats;
    }

    public void patchSeatStatus(Long concertDateId, Long seatId, SeatStatus status) {
        Seat seat = concertRepository.findSeatByConcertDateIdAndSeatId(concertDateId, seatId)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, null, Level.WARN));
        seat.patchStatus(status);
    }

}

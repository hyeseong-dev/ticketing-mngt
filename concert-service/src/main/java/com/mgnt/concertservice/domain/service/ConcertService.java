package com.mgnt.concertservice.domain.service;

import com.mgnt.concertservice.controller.response.GetConcertResponse;
import com.mgnt.concertservice.controller.response.GetConcertsResponse;
import com.mgnt.concertservice.controller.response.GetDatesResponse;
import com.mgnt.concertservice.controller.response.GetSeatsResponse;
import com.mgnt.concertservice.domain.entity.Concert;
import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.concertservice.domain.repository.ConcertRepository;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * 콘서트 서비스 클래스
 * <p>
 * 이 클래스는 콘서트와 관련된 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
public class ConcertService implements ConcertInterface {

    private final ConcertRepository concertRepository;
    private final ConcertValidator concertValidator;
    private final TransactionTemplate transactionTemplate;

    @Override
    public List<GetConcertsResponse> getConcerts() {
        List<Concert> concerts = concertRepository.findAll();
        return concerts.stream().map(GetConcertsResponse::from).toList();
    }

    @Override
    public GetConcertResponse getConcert(Long concertId) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND, null, Level.WARN));
        return GetConcertResponse.from(concert);
    }

    @Override
    public GetDatesResponse getDates(Long concertId) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND, null, Level.WARN));
        concertValidator.dateIsNull(concert.getConcertDateList());

        List<GetDatesResponse.DateInfo> dateInfos = new ArrayList<>();
        concert.getConcertDateList().forEach(concertDate -> {
            boolean available = concertRepository.existsByConcertDateAndStatus(concertDate.getConcertDateId(), Seat.Status.AVAILABLE);
            dateInfos.add(GetDatesResponse.DateInfo.from(concertDate, available));
        });

        return new GetDatesResponse(dateInfos);
    }

    @Override
    public GetSeatsResponse getAvailableSeats(Long concertDateId) {
        List<Seat> availableSeats = concertRepository.findSeatsByConcertDateIdAndStatus(concertDateId, Seat.Status.AVAILABLE);
        return GetSeatsResponse.from(availableSeats);
    }

    @Override
    public void patchSeatStatus(Long concertDateId, int seatNum, Seat.Status status) {
        Seat seat = concertRepository.findSeatByConcertDateIdAndSeatNum(concertDateId, seatNum)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, null, Level.WARN));
        seat.patchStatus(status);
    }
}

package com.mgnt.concertservice.domain.service.impl;

import com.mgnt.concertservice.controller.response.GetSeatsResponse;
import com.mgnt.concertservice.domain.entity.Seat;
import com.mgnt.concertservice.domain.repository.SeatRepository;
import com.mgnt.concertservice.domain.service.SeatService;
import com.mgnt.core.enums.SeatStatus;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Seat> getAvailableSeats(Long concertDateId) {
        List<Seat> availableSeats = seatRepository.findAllAvailableSeatsByConcertDateIdAndStatus(concertDateId, SeatStatus.AVAILABLE);
        return availableSeats;
    }

    @Transactional
    public void reserveSeat(Long concertDateId, Long seatId) {
        Optional<Seat> seatOptional = seatRepository.findAndLockByConcertDateIdAndSeatId(concertDateId, seatId);
        Seat seat = seatOptional.orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, null, Level.WARN));

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new CustomException(ErrorCode.SEAT_NOT_AVAILABLE, null, Level.WARN);
        }

        seat.patchStatus(SeatStatus.RESERVED);
        seatRepository.save(seat);
    }

    @Transactional(readOnly = true)
    public Seat getSeatInfo(Long concertDateId, Long seatId) {
        return seatRepository.findByConsertDateIdAndSeatId(concertDateId, seatId)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, null, Level.WARN));
    }

//    @Transactional
//    public void updateSeatStatus(Long concertDateId, Long seatId, SeatStatus newStatus) {
//        int updatedCount = seatRepository.updateSeatStatus(concertDateId, seatId, newStatus);
//        if (updatedCount == 0) {
//            throw new CustomException(ErrorCode.SEAT_UPDATE_FAILED, null, Level.ERROR);
//        }
//    }

    @Transactional(readOnly = true)
    public List<Seat> getAllSeatsByConcertDateId(Long concertDateId) {
        List<Seat> result = seatRepository.findAllByConcertDateId(concertDateId);
        return result;
    }
}
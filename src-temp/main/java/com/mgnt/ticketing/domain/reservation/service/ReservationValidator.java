package com.mgnt.ticketing.domain.reservation.service;

import com.mgnt.ticketing.base.exception.CustomException;
import com.mgnt.ticketing.domain.reservation.ReservationExceptionEnum;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationValidator {

    private final ReservationRepository reservationRepository;

    public void checkReserved(Long concertDateId, int seatNum) {
        Reservation reservation = reservationRepository.findOneByConcertDateIdAndSeatNum(concertDateId, seatNum);
        // 이미 선택된 좌석
        if (reservation != null
                && List.of(Reservation.Status.RESERVED, Reservation.Status.ING).contains(reservation.getStatus())) {
            throw new CustomException(ReservationExceptionEnum.ALREADY_RESERVED, null, LogLevel.INFO);
        }
    }

    public void isNull(Reservation reservation) {
        // 예약 정보 없음
        if (reservation == null) {
            throw new CustomException(ReservationExceptionEnum.IS_NULL, null, LogLevel.INFO);
        }
    }
}
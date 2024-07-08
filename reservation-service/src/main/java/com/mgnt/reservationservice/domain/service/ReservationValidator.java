package com.mgnt.reservationservice.domain.service;

import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.exception.CustomException;
import com.mgnt.reservationservice.domain.entity.Reservation;
import com.mgnt.reservationservice.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
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
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_RESERVED, null, Level.INFO);
        }
    }

    public void isNull(Reservation reservation) {
        // 예약 정보 없음
        if (reservation == null) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_FOUND, null, Level.INFO);
        }
    }
}
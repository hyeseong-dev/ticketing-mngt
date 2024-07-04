package com.mgnt.ticketing.domain.reservation.service;


import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.concert.service.ConcertService;
import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.payment.repository.PaymentRepository;
import com.mgnt.ticketing.domain.payment.service.PaymentReader;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.reservation.repository.ReservationRepository;
import com.mgnt.ticketing.domain.reservation.service.dto.OccupyTempReservationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 예약 모니터링 클래스
 * <p>
 * 예약 요청을 5분 동안 임시 점유하고, 5분 후 상태를 추적하여 점유 해제 처리하는 역할을 합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationMonitor {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentReader paymentReader;
    private final ConcertService concertService;

    // 예약 요청을 저장할 큐
    private final Queue<OccupyTempReservationDto> tempReservationQueue = new ConcurrentLinkedQueue<>();

    /**
     * 예약을 임시 점유합니다.
     *
     * @param reservationId 예약 ID
     */
    public void occupyReservation(Long reservationId) {
        // 예약 요청을 큐에 추가
        OccupyTempReservationDto occupyDto = OccupyTempReservationDto.toOccupy(reservationId);
        tempReservationQueue.add(occupyDto);
    }

    /**
     * 예약 모니터링을 수행합니다.
     */
    public void reservationMonitoring() {
        new Thread(() -> {
            while (true) {
                long currentTime = System.currentTimeMillis();
                OccupyTempReservationDto occupyDto = tempReservationQueue.peek(); // 큐의 첫 번째 요소
                // 큐의 첫 번째 요소가 5분 경과했을 시
                if (occupyDto != null && (currentTime - occupyDto.occupyTime() >= (5 * 60 * 1000))) {
                    // 예약, 결제 상태 확인
                    Reservation reservation = reservationRepository.findById(occupyDto.reservationId());
                    Payment payment = paymentReader.findPaymentByReservation(reservation);
                    if ((reservation != null && reservation.getStatus().equals(Reservation.Status.ING))
                            && (payment == null)) {
                        // 임시 점유 해제: 완료되지 않은 예약 취소
                        concertService.patchSeatStatus(reservation.getConcertDateId(), reservation.getSeatNum(), Seat.Status.AVAILABLE);
                        reservationRepository.delete(reservation);
                        log.info("완료되지 않은 예약 취소: {}", occupyDto.reservationId());
                        // 처리된 요청을 큐에서 제거
                        tempReservationQueue.remove();
                    }
                }

                try {
                    Thread.sleep(100); // 0.1초 대기
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

}

package com.mgnt.ticketing.controller.reservation.dto;

import com.mgnt.ticketing.controller.reservation.dto.request.CancelRequest;
import com.mgnt.ticketing.controller.reservation.dto.request.ReserveRequest;
import com.mgnt.ticketing.controller.reservation.dto.response.ReserveResponse;
import com.mgnt.ticketing.domain.payment.PaymentEnums;
import com.mgnt.ticketing.domain.reservation.ReservationEnums;
import com.mgnt.ticketing.domain.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;

@RequestMapping("/reservations")
@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;

    @PostMapping("/")
    public ReserveResponse reserve(@RequestBody @Valid ReserveRequest request) {
        // dummy data
        return ReserveResponse.builder()
                .reservationId(1L)
                .status(ReservationEnums.Status.ING)
                .concertInfo(ReserveResponse.ConcertInfo.builder()
                        .concertId(1L)
                        .concertDateId(1L)
                        .name("2024 테스트 콘서트 in 서울")
                        .date(ZonedDateTime.now().plusMonths(1))
                        .seatId(2L)
                        .seatNum(2)
                        .build())
                .paymentInfo(ReserveResponse.PaymentInfo.builder()
                        .paymentId(1L)
                        .status(PaymentEnums.Status.READY)
                        .paymentPrice(79000)
                        .build())
                .build();
    }

    @DeleteMapping("/{reservationId}")
    public void cancel(@PathVariable(value = "reservationId") Long reservationId,
                       @RequestBody @Valid CancelRequest request) {

    }
}
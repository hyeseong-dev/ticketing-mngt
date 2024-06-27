package com.mgnt.ticketing.domain.integration.base;

import com.mgnt.ticketing.domain.concert.entity.Concert;
import com.mgnt.ticketing.domain.concert.entity.ConcertDate;
import com.mgnt.ticketing.domain.concert.entity.Place;
import com.mgnt.ticketing.domain.concert.entity.Seat;
import com.mgnt.ticketing.domain.concert.repository.ConcertRepository;
import com.mgnt.ticketing.domain.concert.repository.PlaceRepository;
import com.mgnt.ticketing.domain.payment.entity.Payment;
import com.mgnt.ticketing.domain.payment.repository.PaymentRepository;
import com.mgnt.ticketing.domain.reservation.entity.Reservation;
import com.mgnt.ticketing.domain.reservation.repository.ReservationRepository;
import com.mgnt.ticketing.domain.user.entity.User;
import com.mgnt.ticketing.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDataHandler {

    private final ConcertRepository concertRepository;
    private final PlaceRepository placeRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    private final EntityManager entityManager;

    // 콘서트 공연장, 좌석 정보 세팅
    public void settingPlaceAndSeats() {
        List<Seat> seats = new ArrayList<>();
        int seatsCnt = 50;
        Place place = Place.builder()
                .name("서울 올림픽경기장")
                .seatsCnt(seatsCnt)
                .build();

        for (int i = 1; i <= seatsCnt; i++) {
            if (i <= 20) {
                seats.add(Seat.builder().place(place).seatNum(i).price(BigDecimal.valueOf(89000)).build());
            } else if (i <= 40) {
                seats.add(Seat.builder().place(place).seatNum(i).price(BigDecimal.valueOf(119000)).build());
            } else {
                seats.add(Seat.builder().place(place).seatNum(i).price(BigDecimal.valueOf(139000)).build());
            }
        }
        placeRepository.addPlace(place);
        placeRepository.addSeats(seats);
    }

    // 콘서트, 회차 정보 세팅
    public void settingConcertAndDate() {
        List<ConcertDate> concertDates = new ArrayList<>();
        concertDates.add(ConcertDate.builder()
                .concertDate(ZonedDateTime.of(LocalDateTime.of(2024, 5, 25, 18, 30), ZoneId.of("Asia/Seoul")))
                .build());
        concertDates.add(ConcertDate.builder()
                .concertDate(ZonedDateTime.of(LocalDateTime.of(2024, 5, 26, 19, 30), ZoneId.of("Asia/Seoul")))
                .build());

        concertRepository.addConcert(Concert.builder()
                .name("아이유 2024 콘서트")
                .placeId(1L)
                .concertDateList(concertDates)
                .build());
        concertRepository.addConcertDates(concertDates);
    }

    // 5, 10번 좌석 예약
    public void reserveSeats() {
        long concertId = 1L;
        long concertDateId = 1L;
        Concert concert = entityManager.find(Concert.class, concertId);
        ConcertDate concertDate = entityManager.find(ConcertDate.class, concertDateId);
        Seat seat_5 = entityManager.find(Seat.class, 5L);
        Seat seat_10 = entityManager.find(Seat.class, 10L);

        reservationRepository.save(Reservation.builder()
                .concert(concert)
                .concertDate(concertDate)
                .seat(seat_5)
                .status(Reservation.Status.ING)
                .reservedAt(ZonedDateTime.now().minusMinutes(3)).build());

        reservationRepository.save(Reservation.builder()
                .concert(concert)
                .concertDate(concertDate)
                .seat(seat_10)
                .status(Reservation.Status.RESERVED)
                .reservedAt(ZonedDateTime.now().minusHours(1)).build());
    }

    // 결제 건 생성
    public void createPayment(Payment.Status status) {
        Reservation reservation = entityManager.find(Reservation.class, 1L);

        paymentRepository.save(Payment.builder()
                .reservation(reservation)
                .price(BigDecimal.valueOf(89000))
                .status(status).build());
    }

    // 유저, 잔액 세팅
    public void settingUser(BigDecimal balance) {
        userRepository.save(new User(1L, balance));
    }
}

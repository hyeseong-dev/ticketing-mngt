package com.mgnt.reservationservice.domain.service;

import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.exception.CustomException;
import com.mgnt.reservationservice.controller.dto.request.CancelRequest;
import com.mgnt.reservationservice.controller.dto.request.ReserveRequest;
import com.mgnt.reservationservice.controller.dto.response.ReserveResponse;
import com.mgnt.reservationservice.domain.ReservationExceptionEnum;
import com.mgnt.reservationservice.domain.entity.Reservation;
import com.mgnt.reservationservice.domain.event.ReservationOccupiedEvent;
import com.mgnt.reservationservice.domain.repository.ReservationRepository;
import com.mgnt.reservationservice.domain.service.dto.GetReservationAndPaymentResDto;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationValidator reservationValidator;
    private final ReservationMonitor reservationMonitor;
    //private final ConcertReader concertReader; // TODO 콘서트 서비스에 트랜잭션 요청
    //private final ConcertService concertService; // TODO 콘서트 서비스에 트랜잭션 요청
    // private final PaymentService paymentService; // TODO 결제 서비스에 트랜잭션 요청
    //private final PaymentReader paymentReader;  // TODO 결제 서비스에 트랜잭션 요청
    //private final ApplicationEventPublisher eventPublisher;

    @PostConstruct
    public void init() {
//        reservationMonitor.reservationMonitoring();
    }

    // 기존 모놀리식 코드에서 데이터 처리를 단일 mysql DB의 여러 테이블을 단일 스프링 서버에서 엔티티클래스들로 서로 참조하며 진행했지만, MSA구조에서는 불가하다.
    // 서비스간 통신을 통해서 이를 해결해야한다. 아래 모놀리식 코드를 어떻게 MSA 구조에 맞게 변경해야 할지 생각해보아야한다. kafka를 통해 통신해야 한다.
    // 토픽이 있어야 한다. 예약 토픽이라고 해야할까>??
    @Transactional
    public ReserveResponse reserve(ReserveRequest request) {
        try {
            /*
            TODO :
                아래 기존 모놀리식에서 사용했던 코드는 추후 conert-service와 통신하여 콘서트의 좌석 정보 상태를 업데이트 해야 한다.
                별도의 토픽을 만들어서 통신해야 하는 것인가? 아니면 단일 토픽에서의 하위 세분화된 무엇으로 통신해야 하는 것인가?
                잘 모르겠다.
             */
            // concertService.patchSeatStatus(request.concertDateId(), request.seatNum(), Seat.Status.DISABLE);

            // validator 현재 서비스(reservation-service)에서 직접적으로 reservation_db와 JPA혹은 queryDSL로 통신하여 처리 가능.
            reservationValidator.checkReserved(request.concertDateId(), request.seatNum());

            // 현재 서비스(reservation-service)에서 직접적으로 reservation_db를 통해 객체 저장
            Reservation reservation = reservationRepository.save(request.toEntity());

            /*
            TODO :
                아래 기존 모놀리식에서 사용했던 코드는 추후 conert-service와 통신하여 콘서트의 좌석 정보 상태를 업데이트 해야 한다.
                별도의 토픽을 만들어서 통신해야 하는 것인가? 아니면 단일 토픽에서의 하위 세분화된 무엇으로 통신해야 하는 것인가?
                잘 모르겠다.
             */
            Concert concert = concertReader.findConcert(reservation.getConcertId());
            ConcertDate concertDate = concertReader.findConcertDate(reservation.getConcertDateId());
            Seat seat = concertReader.findSeat(reservation.getConcertDateId(), reservation.getSeatNum());

            /*
                TODO: 예약 임시 점유 event 발행

             */
            eventPublisher.publishEvent(new ReservationOccupiedEvent(this, reservation.getReservationId()));

            return ReserveResponse.from(reservation, concert, concertDate, seat);

        } catch (ObjectOptimisticLockingFailureException e) {
            // 락 획득 실패 시
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_RESERVED, null, Level.ERROR);
        }
    }

    public Reservation addReservation(ReserveRequest request) {
        return reservationRepository.save(request.toEntity());
    }

//    @Override
//    @Transactional
//    public void cancel(Long reservationId, CancelRequest request) {
//        Reservation reservation = reservationRepository.findByIdAndUserId(reservationId, request.userId());
//
//        // validator
//        reservationValidator.isNull(reservation);
//
//        Payment payment = paymentReader.findPaymentByReservation(reservation);
//        if (payment != null) {
//            // 결제 내역 존재하면 환불 처리
//            paymentService.cancel(payment.getPaymentId());
//        }
//        reservationRepository.delete(reservation);
//    }
//
//    @Override
//    public List<GetMyReservationsResponse> getMyReservations(Long userId) {
//        List<GetReservationAndPaymentResDto> myReservations = reservationRepository.getMyReservations(userId);
//        return myReservations.stream().map(GetMyReservationsResponse::from).toList();
//    }
}

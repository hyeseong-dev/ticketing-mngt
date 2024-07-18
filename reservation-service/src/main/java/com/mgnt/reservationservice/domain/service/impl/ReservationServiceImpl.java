package com.mgnt.reservationservice.domain.service.impl;

import com.mgnt.core.enums.ReservationStatus;
import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.event.reservation_service.QueueEventStatus;
import com.mgnt.core.event.reservation_service.ReservationRequestedEvent;
import com.mgnt.core.exception.CustomException;
import com.mgnt.reservationservice.controller.dto.request.TokenRequestDTO;
import com.mgnt.reservationservice.controller.dto.response.ReservationResponseDTO;
import com.mgnt.reservationservice.controller.dto.response.TokenResponseDTO;
import com.mgnt.reservationservice.domain.entity.Reservation;
import com.mgnt.reservationservice.domain.repository.QueueRedisRepository;
import com.mgnt.reservationservice.domain.repository.ReservationRedisRepository;
import com.mgnt.reservationservice.domain.repository.ReservationRepository;
import com.mgnt.reservationservice.domain.service.ReservationService;
import com.mgnt.reservationservice.domain.service.ReservationValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static com.mgnt.reservationservice.utils.Constants.ACCESS_TOKEN_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ReservationRepository reservationRepository;
    private final ReservationValidator reservationValidator;
    private final ReservationRedisRepository reservationRedisRepository;
    private final QueueRedisRepository queueRedisRepository;
    private final Long TEMP_RESERVATION_SECONDS = 300L; // 5분
    private final Long TEMP_RESERVATION_MINUTES = 5L; // 5분


    @Override
    public TokenResponseDTO getTokenStatus(Long userId, TokenRequestDTO request) {
        String tokenKey = String.format(ACCESS_TOKEN_KEY, userId, request.concertId(), request.concertDateId());
        String token = queueRedisRepository.getAccessToken(tokenKey);
        Long ttlSeconds = queueRedisRepository.getAccessTokenTTL(tokenKey);

        log.info("Checking token status for user {}. Token: {}, TTL: {}", userId, token, ttlSeconds);
        ZonedDateTime expiryTime = null;
        if (ttlSeconds != null && ttlSeconds > 0) {
            expiryTime = ZonedDateTime.now().plus(ttlSeconds, ChronoUnit.SECONDS);
        }

        if (token != null) {
            return new TokenResponseDTO(userId, request.concertId(), request.concertDateId(),
                    QueueEventStatus.READY, null, token, expiryTime);
        } else {
            return new TokenResponseDTO(userId, request.concertId(), request.concertDateId(),
                    QueueEventStatus.WAITING, null, null, null);
        }
    }

    public List<ReservationResponseDTO> getMyReservations(Long userId) {
        // 먼저 Redis에서 캐시된 데이터 조회
        List<ReservationResponseDTO> cachedReservations = reservationRedisRepository.getUserReservations(userId);

        if (cachedReservations != null && !cachedReservations.isEmpty()) {
            log.info("캐시에서 예약 정보 조회: userId={}, count={}", userId, cachedReservations.size());
            return cachedReservations;
        }

        // 캐시에 데이터가 없으면 DB에서 조회
        List<Reservation> reservations = reservationRepository.findAllByUserId(userId);
        log.info("DB에서 예약 정보 조회: userId={}, count={}", userId, reservations.size());

        List<ReservationResponseDTO> responseDTOs = reservations.stream()
                .map(ReservationResponseDTO::from)
                .collect(Collectors.toList());

        // 조회한 데이터를 Redis에 캐시
        reservationRedisRepository.saveUserReservations(userId, responseDTOs);

        return responseDTOs;
    }


    private ReservationResponseDTO convertToDTO(Reservation reservation) {
        return new ReservationResponseDTO(
                reservation.getReservationId(),
                reservation.getStatus(),
                reservation.getUserId(),
                reservation.getConcertId(),
                reservation.getConcertDateId(),
                reservation.getSeatId(),
                reservation.getPrice(),
                reservation.getReservedAt()
        );
    }

    @Transactional
    public void updateReservation(Long reservationId, ReservationStatus newStatus) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));

        reservation.updateStatus(newStatus);
        reservationRepository.save(reservation);

        // 캐시 업데이트
        Long userId = reservation.getUserId();
        List<ReservationResponseDTO> userReservations = reservationRedisRepository.getUserReservations(userId);

        if (userReservations != null) {
            List<ReservationResponseDTO> updatedReservations = userReservations.stream()
                    .map(dto -> dto.reservationId().equals(reservationId) ? convertToDTO(reservation) : dto)
                    .collect(Collectors.toList());

            reservationRedisRepository.saveUserReservations(userId, updatedReservations);
        }
    }

    // ========================================================================= API 구분선
    @Override
    public void initiateReservation(Long userId, String reservationToken, Long xUserId, Long concertId, Long concertDateId, Long seatId) {

        String seatKey = String.format("seat:%d:%d", seatId, concertDateId);
        log.info("Initiating reservation for user {} for seat {}", userId, seatKey);

        try {
            // Redis에서 현재 좌석 상태 확인
            String currentStatus = reservationRedisRepository.get(seatKey);

            if (currentStatus == null) {
                log.warn("Seat {} not found in Redis", seatKey);
                throw new CustomException(ErrorCode.SEAT_NOT_FOUND, null, Level.WARN);
            }

            if (!currentStatus.equals("AVAILABLE")) {
                log.warn("Seat {} is not available. Current status: {}", seatKey, currentStatus);
                throw new CustomException(ErrorCode.SEAT_NOT_AVAILABLE, null, Level.WARN);
            }

            // Redis에 임시로 좌석 상태 및 사용자 정보 저장
            String pendingValue = String.format("PENDING:%d", userId);
            boolean setSuccess = reservationRedisRepository.setTempSeat(seatKey, pendingValue, TEMP_RESERVATION_MINUTES);

            if (!setSuccess) {
                log.warn("Failed to set temporary reservation for seat {}", seatKey);
                throw new CustomException(ErrorCode.RESERVATION_ALREADY_RESERVED, null, Level.WARN);
            }

            // Kafka를 통해 ConcertService에 좌석 상태 업데이트 요청
            ReservationRequestedEvent event = new ReservationRequestedEvent(concertDateId, userId, concertId, seatId);
            kafkaTemplate.send("reservation-requests", event)
                    .thenAccept(result -> log.info("Successfully sent reservation request event for seat {}", seatKey))
                    .exceptionally(ex -> {
                        log.error("Failed to send reservation request event for seat {}", seatKey, ex);
                        // 여기서 Redis에 저장된 임시 예약 정보를 롤백해야 할 수 있습니다.
                        reservationRedisRepository.delete(seatKey);
                        throw new CompletionException(new CustomException(ErrorCode.SEAT_RESERVATION_REQUEST_FAILED, null, Level.ERROR));
                    });

            log.info("Reservation initiated successfully for user {} for seat {}", userId, seatKey);
        } catch (Exception e) {
            log.error("Error during reservation initiation for user {} for seat {}", userId, seatKey, e);
            throw e;
        }
    }
}
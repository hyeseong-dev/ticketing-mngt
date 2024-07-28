package com.mgnt.reservationservice.kafka;

import com.mgnt.core.error.ErrorCode;
import com.mgnt.core.event.reservation_service.ReservationRequestedEvent;
import com.mgnt.core.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletionException;

import static com.mgnt.core.constants.Constants.TOPIC_RESERVATION_REQUESTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void initiateReservation(Long userId, String reservationToken, Long xUserId, Long concertId, Long concertDateId, Long seatId) {
        ReservationRequestedEvent event = new ReservationRequestedEvent(
                null, concertDateId, userId, concertId, seatId);
        kafkaTemplate.send(TOPIC_RESERVATION_REQUESTS, event)
                .thenAccept(result -> log.info("Successfully sent reservation request event for seat {}", seatId))
                .exceptionally(ex -> {
                    log.error("Failed to send reservation request event for seat {}", seatId, ex);
                    throw new CompletionException(new CustomException(ErrorCode.SEAT_RESERVATION_REQUEST_FAILED, null, Level.ERROR));
                });
    }
}

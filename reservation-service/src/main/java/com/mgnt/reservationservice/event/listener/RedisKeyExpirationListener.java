package com.mgnt.reservationservice.event.listener;

import com.mgnt.core.event.concert_service.SeatStatusUpdatedEvent;
import com.mgnt.reservationservice.domain.repository.ReservationRedisRepository;
import com.mgnt.core.enums.SeatStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.mgnt.core.constants.Constants.*;

@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private final ReservationRedisRepository reservationRedisRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer,
                                      ReservationRedisRepository reservationRedisRepository,
                                      KafkaTemplate<String, Object> kafkaTemplate) {
        super(listenerContainer);
        this.reservationRedisRepository = reservationRedisRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    protected void doHandleMessage(Message message) {
        String expiredKey = message.toString();
        log.debug("Received expiration event for key: {}", expiredKey);

        if (expiredKey.startsWith(EXPIRY_KEY)) {
            log.info("Handling expiration for temporary reservation: {}", expiredKey);
            handleTempReservationExpiration(expiredKey);
        }
    }

    private void handleTempReservationExpiration(String expiredKey) {
        try {
            String[] keyParts = expiredKey.split(":");
            if (keyParts.length != 3) {
                log.error("Invalid key format: {}", expiredKey);
                return;
            }

            Long seatId = Long.parseLong(keyParts[2]);
            String seatKey = SEAT_KEY_PREFIX + seatId;

            String seatInfo = reservationRedisRepository.get(seatKey);
            if (seatInfo == null) {
                log.warn("Seat information not found in Redis for key: {}", seatKey);
                return;
            }

            // 좌석 상태를 AVAILABLE로 변경
            reservationRedisRepository.updateSeatStatus(seatId, SeatStatus.AVAILABLE);

            // Kafka 이벤트 발행
            SeatStatusUpdatedEvent event = new SeatStatusUpdatedEvent(
                    null, null, null, null, seatId, null, SeatStatus.AVAILABLE
            );
            kafkaTemplate.send(TOPIC_SEAT_STATUS_UPDATES, event);

            log.info("Temporary reservation expired and seat status reset for seatId: {}", seatId);
        } catch (Exception e) {
            log.error("Error handling temporary reservation expiration", e);
        }
    }
}
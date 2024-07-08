package com.mgnt.paymentservice.domain.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class HelloEventConsumer {

    @KafkaListener(topics = "hello", groupId = "group_id")
    public void consume(@Payload Map<String, String> message) {
        log.info("Consumed message: {}", message);
        // Process the message
    }
}
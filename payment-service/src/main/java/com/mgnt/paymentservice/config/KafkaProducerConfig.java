package com.mgnt.paymentservice.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaProducerConfig {

    private final static String TRANSACTIONAL_ID = "TS_ID-";
    private final static String BOOTSTRAP_SERVER = "localhost:9092,kafka:9092";
    private final static int RETRIES_CONFIG = 3;
    private final static String ACKS_CONFIG = "all";

    @Value("${kafka.producer.idempotence:true}")
    private boolean ENABLE_IDEMPOTENCE_CONFIG;


    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, ENABLE_IDEMPOTENCE_CONFIG);
        configProps.put(ProducerConfig.ACKS_CONFIG, ACKS_CONFIG);
        configProps.put(ProducerConfig.RETRIES_CONFIG, RETRIES_CONFIG);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    public KafkaTemplate<String, Object> kafkaTemplateWithTransactionId() {
        Map<String, Object> configProps = new HashMap<>(producerFactory().getConfigurationProperties());
        configProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, TRANSACTIONAL_ID + UUID.randomUUID());
        ProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(configProps);
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}
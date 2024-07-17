package com.mgnt.reservationservice.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@EnableKafka
@Configuration
class KafkaConsumerConfig {

    @Value("${spring.kafka.consumer.group-id}")
    private String GROUP_ID;

    @Value("${spring.kafka.bootstrap-servers}")
    private String BOOTSTRAP_SERVER;

    private final static String AUTO_OFFSET_RESET_CONFIG = "earliest";
    private final static String TRUSTED_PACKAGES = "*";

    @Value("${kafka.retry.enabled:false}")
    private boolean retryEnabled;

    @Value("${kafka.retry.interval:1000}")
    private long retryInterval;

    @Value("${kafka.retry.max-attempts:3}")
    private int maxAttempts;

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET_CONFIG);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class.getName());
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, TRUSTED_PACKAGES);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }


    @Bean
    public CommonErrorHandler errorHandler() {
        FixedBackOff fixedBackOff = new FixedBackOff(retryInterval, maxAttempts - 1);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler((consumerRecord, exception) -> {
            log.error("처리 중 오류 발생: topic = {}, partition = {}, offset = {}, exception = {}",
                    consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(),
                    exception.getMessage());
        }, retryEnabled ? fixedBackOff : new FixedBackOff(0, 0));

        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("재시도 중 실패한 레코드: topic = {}, partition = {}, offset = {}, 시도 횟수 = {}",
                        record.topic(), record.partition(), record.offset(), deliveryAttempt)
        );

        errorHandler.addRetryableExceptions(TransientDataAccessException.class);
        errorHandler.addNotRetryableExceptions(NullPointerException.class, IllegalArgumentException.class);

        return errorHandler;
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setMissingTopicsFatal(false);
        return factory;
    }
}

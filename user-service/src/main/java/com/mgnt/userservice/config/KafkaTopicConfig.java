package com.mgnt.userservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic createTopic() {
        return new NewTopic("hello", 1, (short) 1); // 토픽명, 파티션수, 복제인수
    }
}
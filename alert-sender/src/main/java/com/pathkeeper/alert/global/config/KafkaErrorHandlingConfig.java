package com.pathkeeper.alert.global.config;

import com.pathkeeper.alert.domain.fcm.FcmException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
public class KafkaErrorHandlingConfig {

    @Bean
    public DefaultErrorHandler errorHandler(
            @Qualifier("dlqKafkaTemplate")
            KafkaTemplate<String, Object> dlqKafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                dlqKafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".DLQ", -1)
        );

        // FCM은 외부 API라 더 긴 재시도 정책
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(5);
        backOff.setInitialInterval(2000L);   // 2초
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(30_000L);     // 최대 30초

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // FcmException은 재시도 가능한 예외로 등록
        handler.addRetryableExceptions(FcmException.class);

        return handler;
    }
}
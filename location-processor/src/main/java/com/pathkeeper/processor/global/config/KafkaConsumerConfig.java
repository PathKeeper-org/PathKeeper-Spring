package com.pathkeeper.processor.global.config;

import com.pathkeeper.common.dto.LocationMessage;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.MicrometerConsumerListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, LocationMessage> locationConsumerFactory(MeterRegistry meterRegistry) {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "location-processor-group");

        // 자동 커밋 비활성화 (수동 ack)
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // 오프셋 리셋 정책
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        // 한 번에 가져올 메시지 수
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        config.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        config.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);

        // Poll 간격 (이 시간 안에 처리 완료 못하면 리밸런싱)
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);  // 5분

        // Heartbeat
        config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 45000);
        config.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);

        // Deserializer
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                ErrorHandlingDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS,
                StringDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                JsonDeserializer.class);

        // JSON Deserializer 설정
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.pathkeeper.common.dto");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.pathkeeper.common.dto.LocationMessage");
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        DefaultKafkaConsumerFactory<String, LocationMessage> factory =
                new DefaultKafkaConsumerFactory<>(config);
        // kafka_consumer_fetch_manager_records_lag 등 JMX 기반 메트릭을 Micrometer에 바인딩
        factory.addListener(new MicrometerConsumerListener<>(meterRegistry));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, LocationMessage>
    kafkaListenerContainerFactory(
            ConsumerFactory<String, LocationMessage> locationConsumerFactory,
            DefaultErrorHandler errorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, LocationMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(locationConsumerFactory);

        // 수동 ack 모드
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        // ErrorHandler 적용 (재시도 + DLQ)
        factory.setCommonErrorHandler(errorHandler);

        // 동시성: 인스턴스당 3개 스레드 (10대 × 3 = 30 파티션 1:1 매핑)
        factory.setConcurrency(3);

        return factory;
    }
}
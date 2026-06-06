package com.pathkeeper.api.global.config;

import com.pathkeeper.common.dto.LocationMessage;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Producer 설정.
 * application.yml로도 가능하지만, 명시적 설정으로 의도를 드러냄.
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * LocationMessage 전용 ProducerFactory.
     * 다른 메시지 타입이 추가되면 별도 ProducerFactory 생성.
     */
    @Bean
    public ProducerFactory<String, LocationMessage> locationMessageProducerFactory() {
        Map<String, Object> config = new HashMap<>();

        // 기본 연결
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // 신뢰성 설정
        config.put(ProducerConfig.ACKS_CONFIG, "1");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        // 성능 최적화
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        config.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

        // 타임아웃
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 30000);

        // Serializer를 인스턴스로 직접 주입 (class 참조 방식 deprecated 대응)
        return new DefaultKafkaProducerFactory<>(
                config,
                new StringSerializer(),
                new JsonSerializer<>()
        );
    }

    /**
     * LocationMessage 전용 KafkaTemplate.
     */
    @Bean
    public KafkaTemplate<String, LocationMessage> locationKafkaTemplate(
            ProducerFactory<String, LocationMessage> locationMessageProducerFactory) {
        return new KafkaTemplate<>(locationMessageProducerFactory);
    }
}

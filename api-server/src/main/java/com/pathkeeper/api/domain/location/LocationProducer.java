package com.pathkeeper.api.domain.location;

import com.pathkeeper.common.constant.KafkaTopics;
import com.pathkeeper.common.dto.LocationMessage;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 위치 메시지를 Kafka로 발행한다.
 * Partition Key를 userId로 설정하여 같은 사용자의 메시지가 같은 파티션에 들어가도록 보장.
 */
@Component
@Slf4j
public class LocationProducer {

    private final KafkaTemplate<String, LocationMessage> kafkaTemplate;
    private final Counter sentCounter;
    private final Counter failedCounter;
    private final Timer publishTimer;

    public LocationProducer(KafkaTemplate<String, LocationMessage> locationKafkaTemplate,
                            MeterRegistry meterRegistry) {
        this.kafkaTemplate = locationKafkaTemplate;

        // 메트릭 초기화
        this.sentCounter = Counter.builder("location.kafka.sent")
                .description("Kafka로 발행 성공한 메시지 수")
                .register(meterRegistry);

        this.failedCounter = Counter.builder("location.kafka.failed")
                .description("Kafka 발행 실패 메시지 수")
                .register(meterRegistry);

        this.publishTimer = Timer.builder("location.kafka.publish.duration")
                .description("Kafka 발행 소요 시간")
                .register(meterRegistry);
    }

    /**
     * 위치 메시지를 비동기로 발행한다.
     * 호출자는 발행 완료를 기다리지 않음.
     */
    public void send(LocationMessage message) {
        Timer.Sample sample = Timer.start();

        // Partition Key = userId (순서 보장)
        String partitionKey = String.valueOf(message.userId());

        CompletableFuture<SendResult<String, LocationMessage>> future =
                kafkaTemplate.send(KafkaTopics.LOCATION_EVENTS, partitionKey, message);

        future.whenComplete((result, ex) -> {
            sample.stop(publishTimer);

            if (ex != null) {
                handleFailure(message, ex);
            } else {
                handleSuccess(message, result);
            }
        });
    }

    private void handleSuccess(LocationMessage message,
                               SendResult<String, LocationMessage> result) {
        sentCounter.increment();

        if (log.isDebugEnabled()) {
            log.debug("Kafka 발행 성공: userId={}, partition={}, offset={}, latency={}ms",
                    message.userId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    System.currentTimeMillis() - message.publishedAt());
        }
    }

    private void handleFailure(LocationMessage message, Throwable ex) {
        failedCounter.increment();

        log.error("Kafka 발행 실패: userId={}, recordedAt={}, error={}",
                message.userId(),
                message.recordedAt(),
                ex.getMessage(),
                ex);

        // TODO: 실패한 메시지를 별도 저장소(Redis, 로컬 파일)에 백업하는 로직 추가 가능
        // saveFallback(message);
    }
}
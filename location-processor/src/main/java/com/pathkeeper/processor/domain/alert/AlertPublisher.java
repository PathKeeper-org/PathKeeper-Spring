package com.pathkeeper.processor.domain.alert;

import com.pathkeeper.common.constant.KafkaTopics;
import com.pathkeeper.common.dto.AlertEvent;
import com.pathkeeper.common.dto.LocationMessage;
import com.pathkeeper.processor.domain.safezone.SafeZoneCacheLoader;
import com.pathkeeper.processor.domain.safezone.SafeZoneInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 이탈 확정 시 알림 이벤트를 Kafka에 발행.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertPublisher {

    private final KafkaTemplate<String, AlertEvent> alertKafkaTemplate;
    private final SafeZoneCacheLoader safeZoneCacheLoader;

    public void publishDeparture(LocationMessage message) {
        Long safeZoneId = safeZoneCacheLoader.getOrLoad(message.userId())
                .map(SafeZoneInfo::getSafeZoneId)
                .orElse(null);

        AlertEvent event = AlertEvent.departure(
                message.userId(),
                safeZoneId,
                message.lat(),
                message.lng(),
                message.publishedAt()
        );

        String partitionKey = String.valueOf(message.userId());

        alertKafkaTemplate.send(KafkaTopics.ALERT_EVENTS, partitionKey, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("알림 발행 실패: userId={}", message.userId(), ex);
                    } else {
                        log.info("이탈 알림 발행: userId={}, partition={}, offset={}",
                                message.userId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
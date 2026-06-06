package com.pathkeeper.processor.consumer;

import com.pathkeeper.common.constant.KafkaTopics;
import com.pathkeeper.common.dto.LocationMessage;
import com.pathkeeper.processor.domain.alert.AlertPublisher;
import com.pathkeeper.processor.domain.buffer.RedisStreamWriter;
import com.pathkeeper.processor.domain.filter.BboxResult;
import com.pathkeeper.processor.domain.filter.BoundingBoxFilter;
import com.pathkeeper.processor.domain.filter.ShortCircuitEvaluator;
import com.pathkeeper.processor.domain.geo.PostGisChecker;
import com.pathkeeper.processor.domain.state.StateMachine;
import com.pathkeeper.processor.domain.state.StateTransition;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 위치 메시지를 받아 안심존 이탈 여부를 판정하고 후속 처리하는 메인 컨슈머.
 *
 * 처리 순서:
 * 1. Bounding Box 검사 (Redis)
 * 2. 단축 평가 (Redis)
 * 3. PostGIS 정밀 검사 (필요 시)
 * 4. 상태 머신 평가 (Redis)
 * 5. 이탈 확정 시 알림 발행 (Kafka)
 * 6. Redis Stream에 경로 저장
 * 7. Kafka offset commit
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LocationConsumer {

    private final BoundingBoxFilter bboxFilter;
    private final ShortCircuitEvaluator shortCircuit;
    private final PostGisChecker postGisChecker;
    private final StateMachine stateMachine;
    private final AlertPublisher alertPublisher;
    private final RedisStreamWriter streamWriter;
    private final MeterRegistry meterRegistry;

    @KafkaListener(
            topics = KafkaTopics.LOCATION_EVENTS,
            groupId = "location-processor-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Timed(value = "location.process", description = "위치 처리 전체 시간")
    public void consume(LocationMessage message, Acknowledgment ack) {
        try {
            // === 1단계: Bounding Box 검사 ===
            BboxResult bboxResult = bboxFilter.check(message);

            // === 2단계: PostGIS 검사 여부 결정 + 실행 ===
            boolean isInside = determineInside(message, bboxResult);

            // === 3단계: 상태 머신 평가 ===
            StateTransition transition = stateMachine.evaluate(message, isInside);

            // === 4단계: 이탈 확정 시 알림 발행 ===
            if (transition == StateTransition.INSIDE_TO_OUTSIDE) {
                alertPublisher.publishDeparture(message);
            }

            // === 5단계: Redis Stream에 경로 저장 (모든 메시지) ===
            streamWriter.append(message);

            // === 6단계: 처리 완료 후 offset commit ===
            ack.acknowledge();

            recordMetrics(bboxResult, isInside, transition);

        } catch (Exception e) {
            log.error("처리 실패: userId={}, recordedAt={}",
                    message.userId(), message.recordedAt(), e);
            throw e;  // ErrorHandler가 재시도/DLQ 처리
        }
    }

    /**
     * Bounding Box 결과에 따라 PostGIS 검사 여부를 결정하고 결과 반환.
     */
    private boolean determineInside(LocationMessage message, BboxResult bboxResult) {
        switch (bboxResult) {
            case OUTSIDE:
                // Bounding Box 밖 → 다각형 안에 있을 수 없음
                return false;

            case UNKNOWN:
                // 안심존 정보 없음 → 일단 안전한 것으로 간주
                // (안심존 등록 안 한 사용자는 이탈 알림 발생 안 시킴)
                return true;

            case INSIDE_BBOX:
                // Bounding Box 안 → 단축 평가 시도 후 PostGIS 검사
                if (shortCircuit.canSkipPostGis(message)) {
                    return true;
                }
                return postGisChecker.contains(message);

            default:
                throw new IllegalStateException("Unknown BboxResult: " + bboxResult);
        }
    }

    private void recordMetrics(BboxResult bboxResult, boolean isInside,
                               StateTransition transition) {
        meterRegistry.counter("location.processed",
                "bbox", bboxResult.name(),
                "inside", String.valueOf(isInside),
                "transition", transition.name()
        ).increment();
    }
}
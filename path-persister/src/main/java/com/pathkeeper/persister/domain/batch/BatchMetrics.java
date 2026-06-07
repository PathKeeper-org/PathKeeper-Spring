package com.pathkeeper.persister.domain.batch;

import com.pathkeeper.persister.global.config.RedisStreamConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Path Persister 메트릭 수집.
 */
@Component
@Slf4j
public class BatchMetrics {

    private final Counter pendingProcessedCounter;
    private final Counter newProcessedCounter;
    private final Counter errorCounter;

    /** Stream 크기 (저장 대기 중인 메시지 수) */
    private final AtomicLong streamSize = new AtomicLong(0);

    /** PEL 크기 (처리 중이지만 ack 안 된 메시지 수) */
    private final AtomicLong pelSize = new AtomicLong(0);

    private final StringRedisTemplate redis;

    public BatchMetrics(MeterRegistry meterRegistry, StringRedisTemplate redis) {
        this.redis = redis;

        this.pendingProcessedCounter = Counter.builder("persister.pending.processed")
                .description("PEL에서 재처리된 메시지 수")
                .register(meterRegistry);

        this.newProcessedCounter = Counter.builder("persister.new.processed")
                .description("새로 처리된 메시지 수")
                .register(meterRegistry);

        this.errorCounter = Counter.builder("persister.batch.errors")
                .description("배치 처리 실패 횟수")
                .register(meterRegistry);

        Gauge.builder("persister.stream.size", streamSize, AtomicLong::get)
                .description("Stream의 메시지 수")
                .register(meterRegistry);

        Gauge.builder("persister.pel.size", pelSize, AtomicLong::get)
                .description("PEL의 메시지 수")
                .register(meterRegistry);
    }

    public void recordPendingProcessed(int count) {
        pendingProcessedCounter.increment(count);
    }

    public void recordNewProcessed(int count) {
        newProcessedCounter.increment(count);
    }

    public void recordBatchError() {
        errorCounter.increment();
    }

    /**
     * Stream/PEL 크기를 주기적으로 갱신.
     */
    @Scheduled(fixedDelay = 30_000)   // 30초마다
    public void updateGauges() {
        try {
            Long size = redis.opsForStream().size(RedisStreamConfig.STREAM_KEY);
            streamSize.set(size != null ? size : 0);

            // PEL 정보 조회
            var pending = redis.opsForStream().pending(
                    RedisStreamConfig.STREAM_KEY,
                    RedisStreamConfig.CONSUMER_GROUP
            );
            pelSize.set(pending != null ? pending.getTotalPendingMessages() : 0);

        } catch (Exception e) {
            log.debug("Gauge 업데이트 실패", e);
        }
    }
}

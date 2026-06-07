package com.pathkeeper.persister.domain.stream;

import com.pathkeeper.persister.global.config.RedisStreamConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Stream 메시지의 ACK와 DELETE 처리.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StreamAcker {

    private final StringRedisTemplate redis;

    /**
     * 처리 완료된 메시지를 ack + delete.
     *
     * - XACK: Consumer Group의 PEL에서 제거
     * - XDEL: Stream 자체에서 메시지 삭제 (메모리 절약)
     */
    public void ackAndDelete(List<String> recordIds) {
        if (recordIds.isEmpty()) {
            return;
        }

        RecordId[] ids = recordIds.stream()
                .map(RecordId::of)
                .toArray(RecordId[]::new);

        try {
            // 1. ACK: PEL에서 제거
            Long acked = redis.opsForStream().acknowledge(
                    RedisStreamConfig.STREAM_KEY,
                    RedisStreamConfig.CONSUMER_GROUP,
                    ids
            );

            // 2. DELETE: Stream에서 완전 제거
            Long deleted = redis.opsForStream().delete(
                    RedisStreamConfig.STREAM_KEY,
                    ids
            );

            log.debug("ACK/DEL 완료: requested={}, acked={}, deleted={}",
                    recordIds.size(), acked, deleted);

        } catch (Exception e) {
            log.error("ACK/DEL 실패: recordIds={}", recordIds, e);
            // 실패해도 throw 안 함: 데이터는 이미 DB에 저장됨
            // 다음 사이클에 다시 ack 시도됨 (idempotent)
        }
    }
}
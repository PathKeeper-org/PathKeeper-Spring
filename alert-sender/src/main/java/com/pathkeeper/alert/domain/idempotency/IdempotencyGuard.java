package com.pathkeeper.alert.domain.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis SETNX로 중복 처리 방지.
 *
 * 키 패턴: notification:{topic}:{partition}:{offset}
 * 같은 Kafka 메시지(같은 partition/offset)가 두 번 처리되는 것을 방지.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyGuard {

    private static final String KEY_PREFIX = "notification:";
    private static final Duration KEY_TTL = Duration.ofDays(1);  // 1일 후 자동 삭제

    private final StringRedisTemplate redis;

    /**
     * 처리 시도. 이미 처리된 적이 있으면 false 반환.
     *
     * @return true: 처리 가능 (이번이 처음), false: 이미 처리됨 (스킵)
     */
    public boolean tryAcquire(String topic, int partition, long offset) {
        String key = buildKey(topic, partition, offset);

        Boolean acquired = redis.opsForValue().setIfAbsent(key, "1", KEY_TTL);

        if (Boolean.TRUE.equals(acquired)) {
            log.debug("멱등성 키 획득: {}", key);
            return true;
        } else {
            log.warn("중복 메시지 감지, 처리 스킵: {}", key);
            return false;
        }
    }

    /**
     * FCM 전송 실패 시 키 삭제 (재시도 시 다시 처리 가능하도록).
     */
    public void release(String topic, int partition, long offset) {
        String key = buildKey(topic, partition, offset);
        redis.delete(key);
        log.debug("멱등성 키 해제: {}", key);
    }

    private String buildKey(String topic, int partition, long offset) {
        return KEY_PREFIX + topic + ":" + partition + ":" + offset;
    }
}
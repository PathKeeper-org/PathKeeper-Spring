package com.pathkeeper.processor.domain.buffer;

import com.pathkeeper.common.dto.LocationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 모든 위치 메시지를 Redis Stream에 저장.
 * Path Persister가 배치로 꺼내서 PostgreSQL에 INSERT.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisStreamWriter {

    private static final String STREAM_KEY = "location:buffer";

    private final StringRedisTemplate redis;

    public void append(LocationMessage message) {
        Map<String, String> entry = new HashMap<>();
        entry.put("userId", String.valueOf(message.userId()));
        entry.put("lat", String.valueOf(message.lat()));
        entry.put("lng", String.valueOf(message.lng()));
        entry.put("batteryLevel", message.batteryLevel() != null
                ? String.valueOf(message.batteryLevel()) : "");
        entry.put("recordedAt", String.valueOf(message.recordedAt()));
        entry.put("publishedAt", String.valueOf(message.publishedAt()));

        try {
            RecordId recordId = redis.opsForStream().add(STREAM_KEY, entry);
            log.trace("Redis Stream 저장: userId={}, recordId={}",
                    message.userId(), recordId);
        } catch (Exception e) {
            log.error("Redis Stream 저장 실패: userId={}", message.userId(), e);
            throw e;  // ack 안 되도록 예외 전파
        }
    }
}

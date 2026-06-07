package com.pathkeeper.persister.global.config;

import io.lettuce.core.RedisCommandExecutionException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis Stream Consumer Group 초기화.
 * 앱 시작 시 1회 실행.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisStreamConfig {

    public static final String STREAM_KEY = "location:buffer";
    public static final String CONSUMER_GROUP = "path-persister-group";
    public static final String CONSUMER_NAME = "persister-1";   // 인스턴스 식별자

    private final StringRedisTemplate redis;

    @PostConstruct
    public void initializeConsumerGroup() {
        try {
            // Consumer Group 생성. MKSTREAM 옵션으로 Stream 자체도 자동 생성.
            redis.opsForStream().createGroup(
                    STREAM_KEY,
                    ReadOffset.from("0"),    // Stream의 처음부터 (기존 메시지도 포함)
                    CONSUMER_GROUP
            );
            log.info("Consumer Group 생성: stream={}, group={}", STREAM_KEY, CONSUMER_GROUP);

        } catch (RedisCommandExecutionException e) {
            // 이미 존재하면 무시
            if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                log.info("Consumer Group 이미 존재: {}", CONSUMER_GROUP);
            } else {
                throw e;
            }
        }
    }
}
package com.pathkeeper.persister.domain.stream;

import com.pathkeeper.persister.domain.location.LocationLogRecord;
import com.pathkeeper.persister.global.config.RedisStreamConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Redis Stream에서 위치 데이터를 읽는다.
 * PEL과 새 메시지를 구분해서 처리.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StreamReader {

    private final StringRedisTemplate redis;

    /**
     * PEL의 미처리 메시지 읽기.
     * 이전에 받았지만 ack 안 한 메시지가 있다면 재처리 대상.
     */
    public List<LocationLogRecord> readPending(int count) {
        return read(ReadOffset.from("0"), count);
    }

    /**
     * 새 메시지 읽기.
     * PEL이 비어있을 때 신규 메시지를 가져옴.
     */
    public List<LocationLogRecord> readNew(int count) {
        return read(ReadOffset.lastConsumed(), count);
    }

    private List<LocationLogRecord> read(ReadOffset offset, int count) {
        StreamReadOptions options = StreamReadOptions.empty()
                .count(count)
                .block(Duration.ofMillis(100));  // 100ms 블로킹

        Consumer consumer = Consumer.from(
                RedisStreamConfig.CONSUMER_GROUP,
                RedisStreamConfig.CONSUMER_NAME
        );

        StreamOffset<String> streamOffset = StreamOffset.create(
                RedisStreamConfig.STREAM_KEY, offset
        );

        try {
            @SuppressWarnings("unchecked")
            List<MapRecord<String, String, String>> records
                    = (List<MapRecord<String, String, String>>)
                    (List<?>) redis.opsForStream().read(consumer, options, streamOffset);

            if (records == null || records.isEmpty()) {
                return Collections.emptyList();
            }

            return parseRecords(records);

        } catch (Exception e) {
            log.error("Stream 읽기 실패", e);
            return Collections.emptyList();
        }
    }

    private List<LocationLogRecord> parseRecords(
            List<MapRecord<String, String, String>> records) {

        List<LocationLogRecord> result = new ArrayList<>(records.size());
        int parseFailCount = 0;

        for (MapRecord<String, String, String> record : records) {
            try {
                LocationLogRecord parsed = LocationLogRecord.from(
                        record.getId().getValue(),
                        record.getValue()
                );
                result.add(parsed);
            } catch (Exception e) {
                parseFailCount++;
                log.warn("파싱 실패, 건너뜀: recordId={}", record.getId(), e);
                // TODO: 파싱 실패 메시지는 별도 처리 필요 (DLQ 등)
                // 일단은 ack되지 않아 PEL에 남아 다음 사이클에 다시 시도됨
            }
        }

        if (parseFailCount > 0) {
            log.warn("배치에서 파싱 실패 건수: {}", parseFailCount);
        }

        return result;
    }
}
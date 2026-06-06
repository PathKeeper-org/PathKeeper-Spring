package com.pathkeeper.alert.domain.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * departure_events 테이블의 알림 발송 기록.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class DepartureEventRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 이탈 이벤트 기록 (알림 발송 전에 호출).
     */
    public Long insertDeparture(Long userId, Long safeZoneId,
                                Double lat, Double lng, long occurredAtMilli) {
        String sql = """
            INSERT INTO departure_events 
                (user_id, safe_zone_id, departed_lat, departed_lng, departed_at, notified)
            VALUES (?, ?, ?, ?, ?, false)
            RETURNING departure_event_id
            """;

        return jdbcTemplate.queryForObject(sql, Long.class,
                userId,
                safeZoneId,
                lat,
                lng,
                new Timestamp(occurredAtMilli)
        );
    }

    /**
     * 알림 발송 완료 표시.
     */
    public void markNotified(Long departureEventId) {
        String sql = """
            UPDATE departure_events 
            SET notified = true, notified_at = ?
            WHERE departure_event_id = ?
            """;

        int updated = jdbcTemplate.update(sql,
                Timestamp.valueOf(LocalDateTime.now()),
                departureEventId
        );

        if (updated == 0) {
            log.warn("DepartureEvent 업데이트 실패: id={}", departureEventId);
        }
    }
}
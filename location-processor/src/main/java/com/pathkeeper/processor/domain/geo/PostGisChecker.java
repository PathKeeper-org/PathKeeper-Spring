package com.pathkeeper.processor.domain.geo;

import com.pathkeeper.common.dto.LocationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * PostGIS를 이용한 다각형 정밀 검사.
 * ST_Contains 함수로 좌표가 안심존 다각형 안에 있는지 정확히 판단.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PostGisChecker {

    private static final String CONTAINS_QUERY = """
        SELECT ST_Contains(
            polygon::geometry, 
            ST_SetSRID(ST_MakePoint(?, ?), 4326)
        )
        FROM safe_zones
        WHERE user_id = ?
        LIMIT 1
        """;

    private final JdbcTemplate jdbcTemplate;

    /**
     * 좌표가 사용자의 안심존 안에 있는지 확인.
     * 안심존이 없으면 false 반환.
     */
    public boolean contains(LocationMessage message) {
        try {
            Boolean result = jdbcTemplate.queryForObject(
                    CONTAINS_QUERY,
                    Boolean.class,
                    message.lng(),    // ST_MakePoint(lng, lat) 순서 주의
                    message.lat(),
                    message.userId()
            );

            return Boolean.TRUE.equals(result);

        } catch (Exception e) {
            log.error("PostGIS 검사 실패: userId={}", message.userId(), e);
            // 실패 시 안전한 쪽으로: false 반환하여 알림 발생 가능성 유지
            // 또는 throw하여 재시도하게 할 수도 있음
            throw e;
        }
    }
}
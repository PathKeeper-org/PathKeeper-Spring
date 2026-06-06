package com.pathkeeper.processor.domain.safezone;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 안심존 정보를 PostgreSQL에서 로드하여 Redis에 캐싱한다.
 * Redis 캐시 미스 시 호출됨.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SafeZoneCacheLoader {

    private static final String CACHE_KEY_PREFIX = "safezone:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redis;

    /**
     * 캐시에서 안심존 조회. 없으면 DB에서 로드하여 캐싱 후 반환.
     */
    public Optional<SafeZoneInfo> getOrLoad(Long userId) {
        // 1. Redis 캐시 조회
        Optional<SafeZoneInfo> cached = getFromCache(userId);
        if (cached.isPresent()) {
            return cached;
        }

        // 2. 캐시 미스 → DB 조회
        Optional<SafeZoneInfo> fromDb = loadFromDatabase(userId);

        // 3. DB에 있으면 캐시에 저장
        fromDb.ifPresent(info -> saveToCache(userId, info));

        return fromDb;
    }

    /**
     * Redis 캐시에서 조회
     */
    private Optional<SafeZoneInfo> getFromCache(Long userId) {
        String key = CACHE_KEY_PREFIX + userId;
        Map<Object, Object> entries = redis.opsForHash().entries(key);

        if (entries.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(SafeZoneInfo.builder()
                    .safeZoneId(Long.parseLong((String) entries.get("safeZoneId")))
                    .userId(userId)
                    .bboxMinLat(Double.parseDouble((String) entries.get("bboxMinLat")))
                    .bboxMaxLat(Double.parseDouble((String) entries.get("bboxMaxLat")))
                    .bboxMinLng(Double.parseDouble((String) entries.get("bboxMinLng")))
                    .bboxMaxLng(Double.parseDouble((String) entries.get("bboxMaxLng")))
                    .build());
        } catch (Exception e) {
            log.warn("Redis 캐시 파싱 실패. 캐시 무효화: userId={}", userId, e);
            redis.delete(key);
            return Optional.empty();
        }
    }

    /**
     * PostgreSQL에서 로드
     */
    private Optional<SafeZoneInfo> loadFromDatabase(Long userId) {
        String sql = """
            
                SELECT safe_zone_id, bbox_min_lat, bbox_max_lat, bbox_min_lng, bbox_max_lng
            FROM safe_zones
            WHERE user_id = ?
            LIMIT 1
            """;

        List<SafeZoneInfo> results = jdbcTemplate.query(sql,
                (rs, rowNum) -> SafeZoneInfo.builder()
                        .safeZoneId(rs.getLong("safe_zone_id"))
                        .userId(userId)
                        .bboxMinLat(rs.getDouble("bbox_min_lat"))
                        .bboxMaxLat(rs.getDouble("bbox_max_lat"))
                        .bboxMinLng(rs.getDouble("bbox_min_lng"))
                        .bboxMaxLng(rs.getDouble("bbox_max_lng"))
                        .build(),
                userId
        );

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Redis에 캐싱
     */
    private void saveToCache(Long userId, SafeZoneInfo info) {
        String key = CACHE_KEY_PREFIX + userId;

        Map<String, String> fields = Map.of(
                "safeZoneId", String.valueOf(info.getSafeZoneId()),
                "bboxMinLat", String.valueOf(info.getBboxMinLat()),
                "bboxMaxLat", String.valueOf(info.getBboxMaxLat()),
                "bboxMinLng", String.valueOf(info.getBboxMinLng()),
                "bboxMaxLng", String.valueOf(info.getBboxMaxLng())
        );

        redis.opsForHash().putAll(key, fields);
        redis.expire(key, CACHE_TTL);

        log.debug("안심존 캐시 저장: userId={}, safeZoneId={}", userId, info.getSafeZoneId());
    }

    /**
     * 캐시 무효화 (안심존 변경 시 외부에서 호출)
     */
    public void invalidate(Long userId) {
        String key = CACHE_KEY_PREFIX + userId;
        redis.delete(key);
        log.info("안심존 캐시 무효화: userId={}", userId);
    }
}

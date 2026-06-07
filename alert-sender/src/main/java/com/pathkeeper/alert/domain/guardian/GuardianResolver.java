package com.pathkeeper.alert.domain.guardian;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 피보호자의 보호자 정보를 조회한다.
 * Redis 캐싱으로 DB 부하 감소.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GuardianResolver {

    private static final String CACHE_KEY_PREFIX = "guardian:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redis;

    /**
     * 피보호자 ID로 모든 보호자 정보 조회 (FCM 토큰 있는 보호자만).
     */
    public List<GuardianInfo> findGuardiansOf(Long protegeId) {
        // 캐시 조회
        List<GuardianInfo> cached = getFromCache(protegeId);
        if (cached != null) {
            return cached;
        }

        // DB 조회
        List<GuardianInfo> guardians = loadFromDatabase(protegeId);

        // 캐싱 (빈 결과도 캐싱하여 DB 폭주 방지)
        saveToCache(protegeId, guardians);

        return guardians;
    }

    /**
     * 보호자의 FCM 토큰 무효화 (잘못된 토큰 응답 받았을 때).
     */
    public void invalidateToken(Long guardianId) {
        String sql = "UPDATE users SET fcm_token = NULL WHERE user_id = ?";
        int updated = jdbcTemplate.update(sql, guardianId);
        log.warn("FCM 토큰 무효화: guardianId={}, updated={}", guardianId, updated);

        // 이 보호자가 속한 모든 피보호자의 캐시 무효화
        // (간단히 전체 캐시 클리어는 부담 → guardian_id로 protege_id를 역조회)
        invalidateRelatedCache(guardianId);
    }

    private List<GuardianInfo> loadFromDatabase(Long protegeId) {
        String sql = """
            SELECT u.user_id, u.name, u.fcm_token
            FROM users u
            JOIN guardian_relations gr ON u.user_id = gr.guardian_id
            WHERE gr.protege_id = ?
            AND u.fcm_token IS NOT NULL
            AND u.role = 'GUARDIAN'
            """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> GuardianInfo.builder()
                        .guardianId(rs.getLong("user_id"))
                        .name(rs.getString("name"))
                        .fcmToken(rs.getString("fcm_token"))
                        .build(),
                protegeId
        );
    }

    private List<GuardianInfo> getFromCache(Long protegeId) {
        String key = CACHE_KEY_PREFIX + protegeId;
        List<String> rawList = redis.opsForList().range(key, 0, -1);

        // null 또는 빈 리스트 = 캐시 미스 (Redis LRANGE는 키 없으면 빈 리스트 반환)
        if (rawList == null || rawList.isEmpty()) {
            return null;
        }

        // 빈 결과 캐싱 마커
        if (rawList.size() == 1 && "EMPTY".equals(rawList.get(0))) {
            return Collections.emptyList();
        }

        List<GuardianInfo> result = new ArrayList<>();
        for (String raw : rawList) {
            try {
                // 형식: guardianId|name|fcmToken
                String[] parts = raw.split("\\|", 3);
                if (parts.length == 3) {
                    result.add(GuardianInfo.builder()
                            .guardianId(Long.parseLong(parts[0]))
                            .name(parts[1])
                            .fcmToken(parts[2])
                            .build());
                }
            } catch (Exception e) {
                log.warn("캐시 파싱 실패, 무효화: {}", key);
                redis.delete(key);
                return null;
            }
        }
        return result;
    }

    private void saveToCache(Long protegeId, List<GuardianInfo> guardians) {
        String key = CACHE_KEY_PREFIX + protegeId;

        if (guardians.isEmpty()) {
            // 빈 결과도 캐싱 (Cache Stampede 방지)
            redis.opsForList().rightPush(key, "EMPTY");
        } else {
            List<String> serialized = guardians.stream()
                    .map(g -> g.getGuardianId() + "|" + g.getName() + "|" + g.getFcmToken())
                    .toList();
            redis.opsForList().rightPushAll(key, serialized.toArray(new String[0]));
        }

        redis.expire(key, CACHE_TTL);
    }

    /**
     * 피보호자 이름 조회. 조회 실패 시 "피보호자" 반환.
     */
    public String findProtegeName(Long protegeId) {
        try {
            String name = jdbcTemplate.queryForObject(
                    "SELECT name FROM users WHERE user_id = ?",
                    String.class,
                    protegeId
            );
            return name != null ? name : "피보호자";
        } catch (Exception e) {
            log.warn("피보호자 이름 조회 실패: protegeId={}", protegeId, e);
            return "피보호자";
        }
    }

    private void invalidateRelatedCache(Long guardianId) {
        List<Long> protegeIds = jdbcTemplate.queryForList(
                "SELECT protege_id FROM guardian_relations WHERE guardian_id = ?",
                Long.class,
                guardianId
        );

        for (Long protegeId : protegeIds) {
            redis.delete(CACHE_KEY_PREFIX + protegeId);
        }
    }
}
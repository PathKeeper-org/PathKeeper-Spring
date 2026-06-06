// ShortCircuitEvaluator.java
package com.pathkeeper.processor.domain.filter;

import com.pathkeeper.common.dto.LocationMessage;
import com.pathkeeper.processor.domain.geo.HaversineCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 단축 평가: 직전 상태가 INSIDE이고 미세 이동만 했다면 PostGIS 호출 생략.
 *
 * 조건:
 * 1. 직전 상태가 INSIDE
 * 2. 직전 좌표에서 현재 좌표까지의 거리가 임계값 미만
 *
 * 만족 시 PostGIS 검사 없이 INSIDE 확정.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShortCircuitEvaluator {

    private static final String STATE_KEY_PREFIX = "user:state:";

    /** 단축 평가 임계 거리 (미터). 이 이하의 이동은 GPS 노이즈로 간주 */
    private static final double SHORT_CIRCUIT_DISTANCE_METERS = 5.0;

    private final StringRedisTemplate redis;
    private final HaversineCalculator haversine;

    public boolean canSkipPostGis(LocationMessage message) {
        String key = STATE_KEY_PREFIX + message.userId();
        Map<Object, Object> state = redis.opsForHash().entries(key);

        if (state.isEmpty()) {
            return false;  // 상태 정보 없음 → 정밀 검사 필요
        }

        // 1. 직전 상태가 INSIDE인지 확인
        String prevStatus = (String) state.get("status");
        if (!"INSIDE".equals(prevStatus)) {
            return false;
        }

        // 2. 직전 좌표 조회
        String lastLatStr = (String) state.get("lastLat");
        String lastLngStr = (String) state.get("lastLng");
        if (lastLatStr == null || lastLngStr == null) {
            return false;
        }

        double lastLat = Double.parseDouble(lastLatStr);
        double lastLng = Double.parseDouble(lastLngStr);

        // 3. 거리 계산
        double distance = haversine.distanceInMeters(
                lastLat, lastLng, message.lat(), message.lng()
        );

        boolean canSkip = distance < SHORT_CIRCUIT_DISTANCE_METERS;

        if (canSkip) {
            log.trace("단축 평가 적용: userId={}, distance={}m", message.userId(), distance);
        }

        return canSkip;
    }
}

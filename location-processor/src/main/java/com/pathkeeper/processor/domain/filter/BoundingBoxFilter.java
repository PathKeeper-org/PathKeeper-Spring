// BoundingBoxFilter.java
package com.pathkeeper.processor.domain.filter;

import com.pathkeeper.common.dto.LocationMessage;
import com.pathkeeper.processor.domain.safezone.SafeZoneCacheLoader;
import com.pathkeeper.processor.domain.safezone.SafeZoneInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Bounding Box 기반 1차 필터링.
 * Redis 캐시로 빠르게 좌표가 다각형의 외접사각형 안에 있는지 확인.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BoundingBoxFilter {

    /** GPS 노이즈 대응을 위한 margin (약 5m, 위도 1도 ≈ 111km 기준) */
    private static final double MARGIN_DEGREES = 0.00005;

    private final SafeZoneCacheLoader safeZoneCacheLoader;

    public BboxResult check(LocationMessage message) {
        Optional<SafeZoneInfo> safeZoneOpt = safeZoneCacheLoader.getOrLoad(message.userId());

        if (safeZoneOpt.isEmpty()) {
            log.debug("안심존 정보 없음: userId={}", message.userId());
            return BboxResult.UNKNOWN;
        }

        SafeZoneInfo safeZone = safeZoneOpt.get();
        boolean inside = safeZone.isInBoundingBox(message.lat(), message.lng(), MARGIN_DEGREES);

        return inside ? BboxResult.INSIDE_BBOX : BboxResult.OUTSIDE;
    }
}
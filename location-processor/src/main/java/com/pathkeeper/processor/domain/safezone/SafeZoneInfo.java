package com.pathkeeper.processor.domain.safezone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 사용자의 안심존 정보 (캐싱용).
 * Redis에 Hash 구조로 저장됨.
 */
@Getter
@Builder
@AllArgsConstructor
public class SafeZoneInfo {

    private final Long safeZoneId;
    private final Long userId;

    // Bounding Box
    private final Double bboxMinLat;
    private final Double bboxMaxLat;
    private final Double bboxMinLng;
    private final Double bboxMaxLng;

    /**
     * 좌표가 Bounding Box 안에 있는지 (margin 적용)
     */
    public boolean isInBoundingBox(double lat, double lng, double marginDegrees) {
        return lat >= (bboxMinLat - marginDegrees)
                && lat <= (bboxMaxLat + marginDegrees)
                && lng >= (bboxMinLng - marginDegrees)
                && lng <= (bboxMaxLng + marginDegrees);
    }
}

// HaversineCalculator.java
package com.pathkeeper.processor.domain.geo;

import org.springframework.stereotype.Component;

/**
 * 두 GPS 좌표 사이의 거리 계산 (Haversine 공식).
 */
@Component
public class HaversineCalculator {

    private static final double EARTH_RADIUS_METERS = 6371000.0;

    /**
     * 두 좌표 사이의 거리를 미터 단위로 반환
     */
    public double distanceInMeters(double lat1, double lng1, double lat2, double lng2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }
}
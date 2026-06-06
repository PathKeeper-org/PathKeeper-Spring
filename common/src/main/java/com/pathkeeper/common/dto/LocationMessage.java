package com.pathkeeper.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Kafka location-events 토픽으로 전달되는 위치 메시지.
 * api-server가 발행하고 location-processor가 소비한다.
 */
public record LocationMessage(
        Long userId,
        Double lat,
        Double lng,
        Integer batteryLevel,
        Long recordedAt,        // 클라이언트 측정 시각 (epoch milli)
        Long publishedAt        // 서버 발행 시각 (epoch milli)
) {

    /**
     * 정적 팩토리 메서드: 서버 측 정보를 자동으로 채움.
     */
    public static LocationMessage of(Long userId, Double lat, Double lng,
                                     Integer batteryLevel, Long recordedAt) {
        return new LocationMessage(
                userId, lat, lng, batteryLevel, recordedAt, System.currentTimeMillis()
        );
    }
}
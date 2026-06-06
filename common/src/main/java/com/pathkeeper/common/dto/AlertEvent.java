package com.pathkeeper.common.dto;

/**
 * 안심존 이탈 알림 이벤트.
 * location-processor가 발행하고 alert-sender가 소비한다.
 */
public record AlertEvent(
        Long userId,
        Long safeZoneId,
        Double lat,
        Double lng,
        Long occurredAt,
        AlertType type
) {

    public static AlertEvent departure(Long userId, Long safeZoneId,
                                       Double lat, Double lng, Long occurredAt) {
        return new AlertEvent(userId, safeZoneId, lat, lng, occurredAt, AlertType.DEPARTURE);
    }

    public enum AlertType {
        DEPARTURE,    // 이탈
        RETURN        // 복귀 (선택적)
    }
}

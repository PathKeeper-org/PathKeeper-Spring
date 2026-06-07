package com.pathkeeper.persister.domain.location;

import lombok.Builder;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

/**
 * Redis Stream에서 읽은 위치 데이터.
 * recordId(Stream의 ID)와 LocationLog로 변환할 정보를 가진다.
 */
@Getter
@Builder
public class LocationLogRecord {

    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final String recordId;       // Redis Stream의 ID (예: "1716527890123-0")
    private final Long userId;
    private final Double lat;
    private final Double lng;
    private final Integer batteryLevel;
    private final Long recordedAtMilli;
    private final Long publishedAtMilli;

    /**
     * Stream에서 읽은 Map을 LocationLogRecord로 파싱.
     * 파싱 실패 시 IllegalArgumentException.
     */
    public static LocationLogRecord from(String recordId, Map<String, String> entry) {
        try {
            return LocationLogRecord.builder()
                    .recordId(recordId)
                    .userId(Long.parseLong(entry.get("userId")))
                    .lat(Double.parseDouble(entry.get("lat")))
                    .lng(Double.parseDouble(entry.get("lng")))
                    .batteryLevel(parseBatteryLevel(entry.get("batteryLevel")))
                    .recordedAtMilli(Long.parseLong(entry.get("recordedAt")))
                    .publishedAtMilli(Long.parseLong(entry.get("publishedAt")))
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Stream 데이터 파싱 실패: recordId=" + recordId + ", entry=" + entry, e);
        }
    }

    /**
     * LocationLog Entity로 변환.
     */
    public LocationLog toEntity() {
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(lng, lat));
        point.setSRID(4326);

        LocalDateTime recordedAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(recordedAtMilli), SEOUL
        );

        return LocationLog.builder()
                .userId(userId)
                .location(point)
                .lat(lat)
                .lng(lng)
                .batteryLevel(batteryLevel)
                .recordedAt(recordedAt)
                .build();
    }

    private static Integer parseBatteryLevel(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

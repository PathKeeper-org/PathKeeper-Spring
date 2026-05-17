package com.pathkeeper.backend.domain.locationLog.service;

import com.pathkeeper.backend.domain.locationLog.LocationLog;
import com.pathkeeper.backend.domain.locationLog.repository.LocationLogRepository;
import com.pathkeeper.backend.domain.user.User;
import com.pathkeeper.backend.global.exception.BusinessException;
import com.pathkeeper.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GpsAnomalyFilter {

    private final LocationLogRepository locationLogRepository;

    // 사람이 이동 가능한 최대 속도: 30 m/s
    // GPS 스파이크는 수백~수천 m/s로 나타나므로 이 임계값으로 충분히 감지 가능
    private static final double MAX_SPEED_MS = 30.0;
    private static final double EARTH_RADIUS_M = 6_371_000.0;

    public void check(User user, double lat, double lng) {
        Optional<LocationLog> lastLogOpt = locationLogRepository.findTopByUserOrderByRecordedAtDesc(user);
        if (lastLogOpt.isEmpty()) {
            return;
        }

        LocationLog lastLog = lastLogOpt.get();
        Point lastPoint = lastLog.getLocation();

        double distanceMeters = haversineDistance(lastPoint.getY(), lastPoint.getX(), lat, lng);

        long elapsedSeconds = Duration.between(lastLog.getRecordedAt(), LocalDateTime.now()).toSeconds();
        // 1초 미만 간격으로 들어온 경우 1초로 올림하여 속도 계산 (0 나눗셈 방지)
        if (elapsedSeconds < 1) {
            elapsedSeconds = 1;
        }

        double speed = distanceMeters / elapsedSeconds;
        if (speed > MAX_SPEED_MS) {
            throw new BusinessException(ErrorCode.GPS_SPEED_ANOMALY);
        }
    }

    private double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_M * c;
    }
}
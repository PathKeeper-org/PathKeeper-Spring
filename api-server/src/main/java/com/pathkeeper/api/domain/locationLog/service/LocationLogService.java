package com.pathkeeper.api.domain.locationLog.service;

import com.pathkeeper.api.domain.locationLog.dto.LatestLocationResponse;
import com.pathkeeper.api.domain.locationLog.dto.LocationSaveRequest;
import com.pathkeeper.api.domain.locationLog.dto.LocationSaveResponse;
import com.pathkeeper.api.domain.locationLog.entity.LocationLog;
import com.pathkeeper.api.domain.locationLog.repository.LocationLogRepository;
import com.pathkeeper.api.domain.safezone.repository.SafeZoneRepository;
import com.pathkeeper.api.domain.user.entity.GuardianRelation;
import com.pathkeeper.api.domain.user.entity.User;
import com.pathkeeper.api.domain.user.repository.GuardianRelationRepository;
import com.pathkeeper.api.domain.user.repository.UserRepository;
import com.pathkeeper.api.global.exception.BusinessException;
import com.pathkeeper.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationLogService {

    private final LocationLogRepository locationLogRepository;
    private final UserRepository userRepository;
    private final SafeZoneRepository safeZoneRepository;
    private final GuardianRelationRepository guardianRelationRepository;
    private final GpsAnomalyFilter gpsAnomalyFilter;

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public LocationSaveResponse saveLocation(String email, LocationSaveRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        validate(request.lat(), request.lng());
        gpsAnomalyFilter.check(user, request.lat(), request.lng());

        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(request.lng(), request.lat()));

        locationLogRepository.save(LocationLog.builder()
                .user(user)
                .location(point)
                .batteryLevel(request.batteryLevel())
                .recordedAt(LocalDateTime.now())
                .build());

        boolean isAlertTriggered = isOutsideSafeZone(user.getId(), request.lat(), request.lng());
        return new LocationSaveResponse(isAlertTriggered);
    }

    public LatestLocationResponse getLatestLocation(String email) {
        User guardian = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        GuardianRelation relation = guardianRelationRepository.findByGuardian(guardian)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTNER_NOT_LINKED));
        User protege = relation.getProtege();

        LocationLog locationLog = locationLogRepository.findTopByUserOrderByRecordedAtDesc(protege)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOCATION_NOT_FOUND));

        Point point = locationLog.getLocation();
        return new LatestLocationResponse(
                point.getY(),
                point.getX(),
                locationLog.getBatteryLevel(),
                locationLog.getCreatedAt()
        );
    }

    private boolean isOutsideSafeZone(Long userId, double lat, double lng) {
        Optional<Integer> result = safeZoneRepository.isPointInSafeZone(userId, lat, lng);
        return result.isPresent() && result.get() == 0;
    }

    private void validate(Double lat, Double lng) {
        if (lat == null || lng == null) {
            throw new BusinessException(ErrorCode.INVALID_GPS_COORDINATE);
        }
        if (lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) {
            throw new BusinessException(ErrorCode.INVALID_GPS_COORDINATE);
        }
    }
}
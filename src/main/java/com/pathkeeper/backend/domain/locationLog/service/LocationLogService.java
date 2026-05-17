package com.pathkeeper.backend.domain.locationLog.service;

import com.pathkeeper.backend.domain.locationLog.LocationLog;
import com.pathkeeper.backend.domain.locationLog.dto.LatestLocationInfo;
import com.pathkeeper.backend.domain.locationLog.dto.LocationSaveCommand;
import com.pathkeeper.backend.domain.locationLog.dto.LocationSaveInfo;
import com.pathkeeper.backend.domain.locationLog.repository.LocationLogRepository;
import com.pathkeeper.backend.domain.safeZone.repository.SafeZoneRepository;
import com.pathkeeper.backend.domain.user.User;
import com.pathkeeper.backend.domain.user.repository.UserRepository;
import com.pathkeeper.backend.global.exception.BusinessException;
import com.pathkeeper.backend.global.exception.ErrorCode;
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
    private final GpsAnomalyFilter gpsAnomalyFilter;

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public LocationSaveInfo saveLocation(String email, LocationSaveCommand command) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        validate(command.lat(), command.lng());
        gpsAnomalyFilter.check(user, command.lat(), command.lng());

        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(command.lng(), command.lat()));

        locationLogRepository.save(LocationLog.builder()
                .user(user)
                .location(point)
                .batteryLevel(command.batteryLevel())
                .recordedAt(LocalDateTime.now())
                .build());

        boolean isAlertTriggered = isOutsideSafeZone(user.getId(), command);
        return new LocationSaveInfo(isAlertTriggered);
    }

    public LatestLocationInfo getLatestLocation(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        User partner = Optional.ofNullable(user.getPartner())
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTNER_NOT_LINKED));

        LocationLog locationLog = locationLogRepository.findTopByUserOrderByRecordedAtDesc(partner)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOCATION_NOT_FOUND));

        Point point = locationLog.getLocation();
        return new LatestLocationInfo(
                point.getY(),
                point.getX(),
                locationLog.getBatteryLevel(),
                locationLog.getCreatedAt()
        );
    }

    private boolean isOutsideSafeZone(Long userId, LocationSaveCommand command) {
        Optional<Integer> result = safeZoneRepository.isPointInSafeZone(
                userId, command.lat(), command.lng()
        );
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
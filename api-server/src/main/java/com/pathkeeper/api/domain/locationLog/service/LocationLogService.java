package com.pathkeeper.api.domain.locationLog.service;

import com.pathkeeper.api.domain.locationLog.dto.LatestLocationResponse;
import com.pathkeeper.api.domain.locationLog.entity.LocationLog;
import com.pathkeeper.api.domain.locationLog.repository.LocationLogRepository;
import com.pathkeeper.api.domain.user.entity.GuardianRelation;
import com.pathkeeper.api.domain.user.entity.User;
import com.pathkeeper.api.domain.user.repository.GuardianRelationRepository;
import com.pathkeeper.api.domain.user.repository.UserRepository;
import com.pathkeeper.api.global.exception.BusinessException;
import com.pathkeeper.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationLogService {

    private final LocationLogRepository locationLogRepository;
    private final UserRepository userRepository;
    private final GuardianRelationRepository guardianRelationRepository;

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
}
package com.pathkeeper.api.domain.safezone.service;

import com.pathkeeper.api.domain.safezone.dto.SafeZoneCoordinate;
import com.pathkeeper.api.domain.safezone.dto.SafeZoneCreateRequest;
import com.pathkeeper.api.domain.safezone.dto.SafeZoneProjection;
import com.pathkeeper.api.domain.safezone.dto.SafeZoneResponse;
import com.pathkeeper.api.domain.safezone.repository.SafeZoneRepository;
import com.pathkeeper.api.domain.user.entity.User;
import com.pathkeeper.api.domain.user.repository.UserRepository;
import com.pathkeeper.api.global.exception.BusinessException;
import com.pathkeeper.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SafeZoneService {

    private final SafeZoneRepository safeZoneRepository;
    private final UserRepository userRepository;

    private static final double RADIUS = 50.0; // 경로로부터 확장할 버퍼 반경 (미터)

    @Transactional
    public SafeZoneResponse generateAndSaveSafeZone(String email, SafeZoneCreateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String wkt = convertToWktLineString(request.path());

        safeZoneRepository.deleteByUser(user);
        safeZoneRepository.saveSafeZoneWithBuffer(user.getId(), request.name(), wkt, RADIUS);

        SafeZoneProjection projection = safeZoneRepository.findSafeZoneInfoByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SAFE_ZONE_NOT_FOUND));

        return new SafeZoneResponse(projection.getId(), projection.getGeoJson());
    }

    public SafeZoneResponse findSafeZone(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return safeZoneRepository.findSafeZoneInfoByUserId(user.getId())
                .map(p -> new SafeZoneResponse(p.getId(), p.getGeoJson()))
                .orElse(new SafeZoneResponse(null, null));
    }

    // 좌표 배열을 순차 연결하는 LINESTRING WKT로 변환
    // (WGS84: ST_GeomFromText의 좌표 순서는 경도(X) 위도(Y))
    private String convertToWktLineString(List<SafeZoneCoordinate> path) {
        String points = path.stream()
                .map(c -> c.longitude() + " " + c.latitude())
                .collect(Collectors.joining(", "));
        return "LINESTRING(" + points + ")";
    }
}
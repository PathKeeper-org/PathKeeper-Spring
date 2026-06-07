package com.pathkeeper.api.domain.safezone.service;

import com.pathkeeper.api.domain.safezone.dto.SafeZoneCoordinate;
import com.pathkeeper.api.domain.safezone.dto.SafeZoneCreateRequest;
import com.pathkeeper.api.domain.safezone.dto.SafeZoneProjection;
import com.pathkeeper.api.domain.safezone.dto.SafeZoneResponse;
import com.pathkeeper.api.domain.safezone.repository.SafeZoneRepository;
import com.pathkeeper.api.domain.user.entity.User;
import com.pathkeeper.api.domain.user.repository.GuardianRelationRepository;
import com.pathkeeper.api.domain.user.repository.UserRepository;
import com.pathkeeper.api.global.exception.BusinessException;
import com.pathkeeper.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final GuardianRelationRepository guardianRelationRepository;
    private final StringRedisTemplate redisTemplate;

    private static final double RADIUS = 50.0; // 경로로부터 확장할 버퍼 반경 (미터)

    // location-processor의 SafeZoneCacheLoader와 동일한 키 패턴
    private static final String SAFEZONE_CACHE_KEY_PREFIX = "safezone:";

    @Transactional
    public SafeZoneResponse generateAndSaveSafeZone(String email, SafeZoneCreateRequest request) {
        User requestUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 안심존은 피보호자의 user_id로 저장
        // location-processor가 피보호자의 userId로 조회하기 때문
        User targetUser = resolveTargetUser(requestUser);

        String wkt = convertToWktLineString(request.path());

        safeZoneRepository.deleteByUser(targetUser);
        safeZoneRepository.saveSafeZoneWithBuffer(targetUser.getId(), request.name(), wkt, RADIUS);

        // location-processor의 Redis 안심존 캐시 무효화 (stale 데이터 방지)
        redisTemplate.delete(SAFEZONE_CACHE_KEY_PREFIX + targetUser.getId());

        SafeZoneProjection projection = safeZoneRepository.findSafeZoneInfoByUserId(targetUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SAFE_ZONE_NOT_FOUND));

        return new SafeZoneResponse(projection.getId(), projection.getGeoJson());
    }

    public SafeZoneResponse findSafeZone(String email) {
        User requestUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        User targetUser = resolveTargetUser(requestUser);

        return safeZoneRepository.findSafeZoneInfoByUserId(targetUser.getId())
                .map(p -> new SafeZoneResponse(p.getId(), p.getGeoJson()))
                .orElse(new SafeZoneResponse(null, null));
    }

    // 보호자가 호출하면 연결된 피보호자 기준, 피보호자가 직접 호출하면 본인 기준
    private User resolveTargetUser(User requestUser) {
        if (requestUser.isGuardian()) {
            return guardianRelationRepository.findByGuardian(requestUser)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARTNER_NOT_LINKED))
                    .getProtege();
        }
        return requestUser;
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
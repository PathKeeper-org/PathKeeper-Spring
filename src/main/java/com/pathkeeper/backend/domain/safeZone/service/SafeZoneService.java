package com.pathkeeper.backend.domain.safeZone.service;

import com.pathkeeper.backend.controller.safeZone.dto.SafeZoneCoordinate;
import com.pathkeeper.backend.controller.safeZone.dto.SafeZoneCreateRequest;
import com.pathkeeper.backend.domain.safeZone.dto.SafeZoneCreateCommand;
import com.pathkeeper.backend.domain.safeZone.dto.SafeZoneCreateInfo;
import com.pathkeeper.backend.domain.safeZone.dto.SafeZoneProjection;
import com.pathkeeper.backend.domain.safeZone.repository.SafeZoneRepository;
import com.pathkeeper.backend.domain.user.User;
import com.pathkeeper.backend.domain.user.repository.UserRepository;
import com.pathkeeper.backend.global.exception.BusinessException;
import com.pathkeeper.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SafeZoneService {

    private final SafeZoneRepository safeZoneRepository;
    private final UserRepository userRepository;

    private static final Long RADIUS = 50L;

    @Transactional
    public SafeZoneCreateInfo generateAndSaveSafeZone(String email, SafeZoneCreateCommand command) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 1. 위경도 리스트를 WKT(LineString)로 변환
        String wkt = convertToWktLineString(command.path());

        safeZoneRepository.deleteByUser(user);

        // 2. 네이티브 쿼리를 통해 연산 및 엔티티 저장
        // (연산 결과를 직접 Polygon 객체로 받아 저장하는 방식)
        safeZoneRepository.saveSafeZoneWithBuffer(user.getId(), wkt, RADIUS);

        // 3. 저장된 결과를 다시 조회하여 GeoJSON으로 반환
        SafeZoneProjection projection = safeZoneRepository.findSafeZoneInfoByUserId(user.getId())
                .orElseThrow(()-> new BusinessException(ErrorCode.SAFE_ZONE_NOT_FOUND));

        return new SafeZoneCreateInfo(projection.getId(), projection.getGeoJson());
    }

    public SafeZoneCreateInfo findSafeZone(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 데이터가 없어도 에러를 던지지 않고 null을 포함한 DTO 반환 예시
        return safeZoneRepository.findSafeZoneInfoByUserId(user.getId())
                .map(p -> new SafeZoneCreateInfo(p.getId(), p.getGeoJson()))
                .orElse(new SafeZoneCreateInfo(null, null));
    }

    private String convertToWktLineString(List<SafeZoneCoordinate> path) {
        /*String points = path.stream()
                .map(p -> p.longitude() + " " + p.latitude()) // 경도 위도 순서
                .collect(Collectors.joining(", "));
        return "LINESTRING(" + points + ")";*/
        List<String> lines = new ArrayList<>();

        for (int i = 0; i < path.size(); i++) {
            for (int j = i + 1; j < path.size(); j++) {
                SafeZoneCoordinate p1 = path.get(i);
                SafeZoneCoordinate p2 = path.get(j);

                String line = "("
                        + p1.longitude() + " " + p1.latitude() + ", "
                        + p2.longitude() + " " + p2.latitude()
                        + ")";

                lines.add(line);
            }
        }

        return "MULTILINESTRING(" + String.join(", ", lines) + ")";
    }
}

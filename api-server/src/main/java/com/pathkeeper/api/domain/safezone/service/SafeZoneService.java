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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SafeZoneService {

    private final SafeZoneRepository safeZoneRepository;
    private final UserRepository userRepository;

    private static final Long RADIUS = 50L;

    @Transactional
    public SafeZoneResponse generateAndSaveSafeZone(String email, SafeZoneCreateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String wkt = convertToWktLineString(request.path());

        safeZoneRepository.deleteByUser(user);

        safeZoneRepository.saveSafeZoneWithBuffer(user.getId(), wkt, RADIUS);

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

    private String convertToWktLineString(List<SafeZoneCoordinate> path) {
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
package com.pathkeeper.backend.domain.safeZone.dto;

import com.pathkeeper.backend.controller.safeZone.dto.SafeZoneCoordinate;
import com.pathkeeper.backend.controller.safeZone.dto.SafeZoneCreateRequest;

import java.util.List;

public record SafeZoneCreateCommand(
        List<SafeZoneCoordinate> path
) {
    public static SafeZoneCreateCommand from(SafeZoneCreateRequest request) {
        return new SafeZoneCreateCommand(
                request.path()
        );
    }
}

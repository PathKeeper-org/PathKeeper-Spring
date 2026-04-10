package com.pathkeeper.backend.domain.safeZone.dto;

import com.pathkeeper.backend.controller.safeZone.dto.SafeZoneCoordinate;
import com.pathkeeper.backend.domain.safeZone.SafeZone;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record SafeZoneCreateInfo(
        Long safeZoneId,
        String geoJson
){
}

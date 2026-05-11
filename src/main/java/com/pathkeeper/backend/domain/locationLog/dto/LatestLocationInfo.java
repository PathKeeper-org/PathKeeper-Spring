package com.pathkeeper.backend.domain.locationLog.dto;

import java.time.LocalDateTime;

public record LatestLocationInfo(
        Double lat,
        Double lng,
        Integer batteryLevel,
        LocalDateTime lastUpdated
) {}
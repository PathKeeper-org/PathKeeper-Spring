package com.pathkeeper.backend.domain.safeZone.dto;

// Repository에서 Native Query 결과를 매핑받을 인터페이스
public interface SafeZoneProjection {
    Long getId();
    String getGeoJson();
}

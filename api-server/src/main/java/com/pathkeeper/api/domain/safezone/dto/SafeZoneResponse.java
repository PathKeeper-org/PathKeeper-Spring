package com.pathkeeper.api.domain.safezone.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "안심존 데이터 응답 DTO")
public record SafeZoneResponse(
        @Schema(description = "안심존 고유 ID", example = "1")
        Long safeZoneId,

        @Schema(description = "안심존 다각형 꼭짓점 좌표 배열")
        String geoJson
) { }
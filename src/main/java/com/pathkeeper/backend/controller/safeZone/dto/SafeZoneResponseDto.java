package com.pathkeeper.backend.controller.safeZone.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "안심존 데이터 응답 DTO")
public record SafeZoneResponseDto (
        @Schema(description = "안심존 고유 ID", example = "1")
        Long safeZoneId,

        @Schema(description = "안심존 알림 활성화 여부", example = "true")
        Boolean isActive,

        @Schema(description = "안심존 다각형 꼭짓점 좌표 배열")
        List<SafeZoneCoordinate> polygon,

        @Schema(description = "마지막 수정 일시", example = "2026-03-30T10:15:30")
        LocalDateTime updatedAt
){ }

package com.pathkeeper.api.domain.safezone.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "안심존 활성화 상태 응답 DTO")
public record SafeZoneActiveResponse (
        @Schema(description = "현재 안심존 알림 활성화 여부", example = "true")
        Boolean isActive
){ }

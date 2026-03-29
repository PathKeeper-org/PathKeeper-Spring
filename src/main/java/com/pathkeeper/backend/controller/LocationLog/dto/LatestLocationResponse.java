package com.pathkeeper.backend.controller.LocationLog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "상대방의 최신 위치 응답 DTO")
public record LatestLocationResponse (
        @Schema(description = "최신 위도", example = "37.402056")
        Double lat,

        @Schema(description = "최신 경도", example = "127.108212")
        Double lng,

        @Schema(description = "최신 배터리 잔량", example = "85")
        Integer batteryLevel,

        @Schema(description = "해당 위치가 서버에 기록된 마지막 시간", example = "2026-03-30T14:30:00")
        LocalDateTime lastUpdated
) { }

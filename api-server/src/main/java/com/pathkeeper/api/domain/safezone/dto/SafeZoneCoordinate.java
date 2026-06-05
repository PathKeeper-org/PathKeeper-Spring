package com.pathkeeper.api.domain.safezone.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "위도 및 경도 좌표 데이터")
public record SafeZoneCoordinate(
        @Schema(description = "위도 (Latitude)", example = "37.402056")
        @NotNull(message = "위도는 필수 값입니다.")
        Double latitude,

        @Schema(description = "경도 (Longitude)", example = "127.108212")
        @NotNull(message = "경도는 필수 값입니다.")
        Double longitude
){}

package com.pathkeeper.backend.controller.LocationLog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "?분 주기 현재 위치 저장 요청 DTO")
public record LocationSaveRequest (
        @Schema(description = "위도 (Latitude)", example = "37.402056")
        @NotNull(message = "위도는 필수 값입니다.")
        Double lat,

        @Schema(description = "경도 (Longitude)", example = "127.108212")
        @NotNull(message = "경도는 필수 값입니다.")
        Double lng,

        @Schema(description = "현재 디바이스 배터리 잔량 (0~100)", example = "80")
        @NotNull(message = "배터리 잔량은 필수입니다.")
        @Min(value = 0, message = "배터리는 0 이상이어야 합니다.")
        @Max(value = 100, message = "배터리는 100 이하이어야 합니다.")
        Integer batteryLevel
){ }
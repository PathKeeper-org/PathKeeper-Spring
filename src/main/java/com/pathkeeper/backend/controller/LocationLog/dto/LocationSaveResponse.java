package com.pathkeeper.backend.controller.LocationLog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "위치 저장 결과 응답 DTO")
public record LocationSaveResponse(
        @Schema(description = "방금 전송한 위치로 인해 안심존 이탈 알림이 발생했는지 여부", example = "false")
        Boolean isAlertTriggered
) { }
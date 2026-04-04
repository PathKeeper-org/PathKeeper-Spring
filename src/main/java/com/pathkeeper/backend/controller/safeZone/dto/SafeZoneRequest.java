package com.pathkeeper.backend.controller.safeZone.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "안심존 생성 및 덮어쓰기 요청 DTO")
public record SafeZoneRequest(
    @Schema(description = "안심존 다각형 꼭짓점 좌표 배열 (순서대로)")
    @NotNull(message = "좌표 배열은 필수입니다.")
    @Valid // 리스트 내부의 CoordinateDto에 걸려있는 @NotNull도 검사하도록 지시
    List<SafeZoneCoordinate> polygon)
{}

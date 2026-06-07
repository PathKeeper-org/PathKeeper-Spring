package com.pathkeeper.api.domain.safezone.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "안심존 생성 및 덮어쓰기 요청 DTO")
public record SafeZoneCreateRequest(
    @Schema(description = "안심존 이름", example = "학교 가는 길")
    @NotBlank(message = "안심존 이름은 필수입니다.")
    @Size(max = 100, message = "안심존 이름은 100자 이하여야 합니다.")
    String name,

    @Schema(description = "경로 좌표 배열 (순서대로, 최소 2개)")
    @NotNull(message = "좌표 배열은 필수입니다.")
    @Size(min = 2, message = "경로는 최소 2개 이상의 좌표가 필요합니다.")
    @Valid
    List<SafeZoneCoordinate> path
) {}

package com.pathkeeper.backend.controller.alertHistory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "단일 알림 내역 응답 DTO")
public record AlertHistoriesResponse(
        @Schema(description = "알림 고유 ID", example = "1")
        Long alertId,

        @Schema(description = "알림 타입", example = "ZONE_OUT")
        String alertType,

        @Schema(description = "알림 메시지 본문", example = "홍길동님이 안심존을 이탈했습니다.")
        String message,

        @Schema(description = "사용자 확인(읽음) 여부", example = "false")
        Boolean isRead,

        @Schema(description = "알림 발생 시간", example = "2026-03-30T16:45:00")
        LocalDateTime createdAt
) { }

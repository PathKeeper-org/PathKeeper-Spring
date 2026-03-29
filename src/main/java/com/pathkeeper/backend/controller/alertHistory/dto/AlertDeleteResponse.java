package com.pathkeeper.backend.controller.alertHistory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 삭제 결과 응답 DTO")
public record AlertDeleteResponse (
        @Schema(description = "삭제된 알림 ID", example = "1")
        Long deletedAlertId,

        @Schema(description = "결과 메시지", example = "알림이 성공적으로 삭제되었습니다.")
        String message
) { }
package com.pathkeeper.api.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "FCM 토큰 업데이트 성공 응답 DTO")
public record FcmTokenResponse (
        @Schema(description = "성공 메시지", example = "FCM 토큰이 업데이트 되었습니다.")
        String message
){ }
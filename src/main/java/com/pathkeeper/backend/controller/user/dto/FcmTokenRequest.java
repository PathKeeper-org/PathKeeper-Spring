package com.pathkeeper.backend.controller.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "FCM 토큰 업데이트 요청 DTO")
public record FcmTokenRequest (
        @Schema(description = "새로운 FCM 토큰", example = "fcm_token_sample_12345...")
        @NotBlank(message = "토큰 값은 비어있을 수 없습니다.")
        String fcmToken
) { }
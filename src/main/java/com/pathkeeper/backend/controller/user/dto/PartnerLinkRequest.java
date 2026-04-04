package com.pathkeeper.backend.controller.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "파트너 연결 요청 DTO")
public record PartnerLinkRequest (
        @Schema(description = "6자리 초대 코드", example = "A3F9K2")
        @NotBlank(message = "초대 코드를 입력해주세요.")
        @Size(min = 6, max = 6, message = "초대 코드는 반드시 6자리여야 합니다.")
        String inviteCode
){ }

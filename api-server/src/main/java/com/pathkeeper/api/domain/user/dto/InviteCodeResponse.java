package com.pathkeeper.api.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "초대 코드 응답 DTO")
public record InviteCodeResponse(
        @Schema(description = "생성된 6자리 초대 코드", example = "A3F9K2")
        String inviteCode
) { }
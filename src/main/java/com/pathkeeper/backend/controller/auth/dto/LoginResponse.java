package com.pathkeeper.backend.controller.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 성공 응답 DTO")
public record LoginResponse(

        @Schema(description = "발급된 Access Token", example = "agwegadfewfawefaw")
        String accessToken
) {}

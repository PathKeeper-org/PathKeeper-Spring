package com.pathkeeper.api.global.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 에러 응답")
public record ErrorResponse(
        @Schema(description = "HTTP 상태 코드", example = "401")
        int status,
        @Schema(description = "커스텀 에러 코드", example = "A001")
        String code,
        @Schema(description = "에러 상세 메세지", example = "인증이 필요합니다.")
        String message
){
}

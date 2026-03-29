package com.pathkeeper.backend.controller.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "파트너 연결 성공 응답 DTO")
public record PartnerLinkResponse (
        @Schema(description = "성공 메시지", example = "파트너 연결이 완료되었습니다.")
        String message,

        @Schema(description = "연결된 상대방의 이름", example = "홍길동")
        String partnerName
){ }

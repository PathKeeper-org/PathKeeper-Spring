package com.pathkeeper.api.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "파트너 연결 성공 응답 DTO")
public record PartnerLinkResponse(
        @Schema(description = "파트너 ID", example = "1")
        Long partnerId,

        @Schema(description = "연결된 상대방의 이름", example = "홍길동")
        String partnerName
) { }
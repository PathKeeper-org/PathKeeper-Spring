package com.pathkeeper.backend.controller.user.dto;

import com.pathkeeper.backend.domain.user.dto.PartnerLinkInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "파트너 연결 성공 응답 DTO")
public record PartnerLinkResponse (
        @Schema(description = "파트너 ID", example = "1")
        Long partnerId,

        @Schema(description = "연결된 상대방의 이름", example = "홍길동")
        String partnerName
){
        public static PartnerLinkResponse from(PartnerLinkInfo info) {
                return new PartnerLinkResponse(
                        info.partnerId(),
                        info.partnerName()
                );
        }
}

package com.pathkeeper.backend.controller.user.dto;

import com.pathkeeper.backend.domain.user.dto.InviteCodeInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "초대 코드 응답 DTO")
public record InviteCodeResponse (
        @Schema(description = "생성된 6자리 초대 코드", example = "A3F9K2")
        String inviteCode
){
        public static InviteCodeResponse from(InviteCodeInfo info) {
                return new InviteCodeResponse(
                        info.inviteCode()
                );
        }
}

package com.pathkeeper.backend.controller.user.dto;

import com.pathkeeper.backend.domain.user.dto.ProfileInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유저 프로필 정보 응답 DTO")
public record UserProfileResponse(
        @Schema(description = "유저 고유 ID", example = "1")
        Long userId,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "역할 (GUARDIAN / PROTEGE)", example = "GUARDIAN")
        String role,

        @Schema(description = "연결된 파트너 이름 (없으면 null)", example = "김철수")
        String partnerName

//        @Schema(description = "초대 코드", example = "A3F9K2")
//        String inviteCode
) {
        public static UserProfileResponse from(ProfileInfo info) {
                return new UserProfileResponse(
                        info.userId(),
                        info.email(),
                        info.name(),
                        info.role(),
                        info.partnerName()
//                        info.inviteCode()
                );
        }
}

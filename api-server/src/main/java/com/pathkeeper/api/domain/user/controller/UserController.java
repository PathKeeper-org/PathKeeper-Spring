package com.pathkeeper.api.domain.user.controller;

import com.pathkeeper.api.domain.user.dto.*;
import com.pathkeeper.api.domain.user.service.UserService;
import com.pathkeeper.api.global.security.details.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "유저 정보, 초대 코드 및 파트너 관리 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 유저의 프로필을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> findProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(userService.findProfile(userDetails.getEmail()));
    }

    @Operation(summary = "초대 코드 생성 및 재발급", description = "보호자가 피보호자를 매핑하기 위한 6자리 랜덤 코드를 생성합니다.")
    @PostMapping("/me/invite-code")
    public ResponseEntity<InviteCodeResponse> generateInviteCode(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(userService.generateInviteCode(userDetails.getEmail()));
    }

    @Operation(summary = "파트너 연결", description = "6자리 초대 코드를 입력하여 피보호자와 보호자가 매핑됩니다.")
    @PostMapping("/me/partner")
    public ResponseEntity<PartnerLinkResponse> linkPartner(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PartnerLinkRequest request) {
        return ResponseEntity.ok(userService.linkPartner(userDetails.getEmail(), request.inviteCode()));
    }

    @Operation(summary = "FCM 토큰 업데이트", description = "앱 실행 시 갱신되는 스마트폰의 푸시 알림용 FCM 토큰을 서버에 저장합니다.")
    @PatchMapping("/me/fcm-token")
    public ResponseEntity<FcmTokenResponse> updateFcmToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FcmTokenRequest request) {
        return ResponseEntity.ok(userService.updateFcmToken(userDetails.getEmail(), request.fcmToken()));
    }
}
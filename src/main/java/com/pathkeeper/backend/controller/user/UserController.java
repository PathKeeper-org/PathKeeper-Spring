package com.pathkeeper.backend.controller.user;

import com.pathkeeper.backend.controller.user.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "유저 정보, 초대 코드 및 파트너 관리 API")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 유저의 프로필을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {

        return ResponseEntity.ok(null); // 임시 반환값
    }

    @Operation(summary = "초대 코드 생성 및 재발급", description = "보호자가 피보호자를 매핑하기 위한 6자리 랜덤 코드를 생성합니다.")
    @PostMapping("/me/invite-code")
    public ResponseEntity<InviteCodeResponse> generateInviteCode() {

        return ResponseEntity.ok(null); // 임시 반환값
    }

    @Operation(summary = "파트너 연결", description = "6자리 초대 코드를 입력하여 피보호자와 보호자가 매핑됩니다.")
    @PostMapping("/me/partner")
    public ResponseEntity<PartnerLinkResponse> linkPartner(
            @Valid @RequestBody PartnerLinkRequest request) {

        return ResponseEntity.ok(null); // 임시 반환값
    }

    @Operation(summary = "FCM 토큰 업데이트", description = "앱 실행 시 갱신되는 스마트폰의 푸시 알림용 FCM 토큰을 서버에 저장합니다.")
    @PatchMapping("/me/fcm-token")
    public ResponseEntity<FcmTokenResponse> updateFcmToken(
            @Valid @RequestBody FcmTokenRequest request) {

        return ResponseEntity.ok(null); // 임시 반환값
    }
}
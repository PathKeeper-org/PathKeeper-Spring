package com.pathkeeper.api.domain.safezone.controller;

import com.pathkeeper.api.domain.safezone.dto.SafeZoneActiveResponse;
import com.pathkeeper.api.domain.safezone.dto.SafeZoneCreateRequest;
import com.pathkeeper.api.domain.safezone.dto.SafeZoneResponse;
import com.pathkeeper.api.domain.safezone.service.SafeZoneService;
import com.pathkeeper.api.global.security.details.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Safe Zone", description = "안심존 설정 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/safe-zones")
public class SafeZoneController {
    private final SafeZoneService safeZoneService;

    @Operation(summary = "안심존 생성", description = "경로들을 병합한 단일 다각형(Polygon) 데이터를 서버에 저장합니다.")
    @PostMapping
    public ResponseEntity<SafeZoneResponse> createSafeZone(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SafeZoneCreateRequest request) {
        return ResponseEntity.ok(safeZoneService.generateAndSaveSafeZone(userDetails.getEmail(), request));
    }

    @Operation(summary = "안심존 조회", description = "등록된 안심존 다각형의 꼭짓점 좌표 배열을 가져옵니다.")
    @GetMapping
    public ResponseEntity<SafeZoneResponse> findSafeZone(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(safeZoneService.findSafeZone(userDetails.getEmail()));
    }

    @Operation(summary = "안심존 활성화/비활성화", description = "특정 상황에서 안심존 이탈 알림을 잠시 꺼둡니다.")
    //@PatchMapping("/active")
    public ResponseEntity<SafeZoneActiveResponse> toggleSafeZoneActive(
            @RequestParam @Parameter(description = "변경할 활성화 상태 (true/false)") boolean isActive) {
        return ResponseEntity.ok(null); // 임시 반환
    }
}
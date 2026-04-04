package com.pathkeeper.backend.controller.safeZone;

import com.pathkeeper.backend.controller.safeZone.dto.SafeZoneActiveResponse;
import com.pathkeeper.backend.controller.safeZone.dto.SafeZoneRequest;
import com.pathkeeper.backend.controller.safeZone.dto.SafeZoneResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Safe Zone", description = "안심존 설정 및 관리 API")
@RestController
@RequestMapping("/api/v1/safe-zones")
public class SafeZoneController {

    @Operation(summary = "안심존 생성 및 덮어쓰기", description = "경로들을 병합한 단일 다각형(Polygon) 데이터를 서버에 저장합니다. 기존 데이터가 있으면 덮어씁니다.")
    @PutMapping
    public ResponseEntity<SafeZoneResponseDto> createOrUpdateSafeZone(
            @Valid @RequestBody SafeZoneRequest request) {

        return ResponseEntity.ok(null); // 임시 반환
    }

    @Operation(summary = "안심존 조회", description = "등록된 안심존 다각형의 꼭짓점 좌표 배열을 가져옵니다.")
    @GetMapping
    public ResponseEntity<SafeZoneResponseDto> getSafeZone() {

        return ResponseEntity.ok(null); // 임시 반환
    }

    @Operation(summary = "안심존 활성화/비활성화", description = "특정 상황에서 안심존 이탈 알림을 잠시 꺼둡니다.")
    @PatchMapping("/active")
    public ResponseEntity<SafeZoneActiveResponse> toggleSafeZoneActive(
            @RequestParam @Parameter(description = "변경할 활성화 상태 (true/false)") boolean isActive) {

        return ResponseEntity.ok(null); // 임시 반환
    }
}
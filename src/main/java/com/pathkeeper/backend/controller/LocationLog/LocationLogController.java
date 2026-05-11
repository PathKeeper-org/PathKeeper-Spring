package com.pathkeeper.backend.controller.LocationLog;

import com.pathkeeper.backend.controller.LocationLog.dto.LatestLocationResponse;
import com.pathkeeper.backend.controller.LocationLog.dto.LocationSaveRequest;
import com.pathkeeper.backend.controller.LocationLog.dto.LocationSaveResponse;
import com.pathkeeper.backend.domain.locationLog.dto.LocationSaveCommand;
import com.pathkeeper.backend.domain.locationLog.dto.LocationSaveInfo;
import com.pathkeeper.backend.domain.locationLog.service.LocationLogService;
import com.pathkeeper.backend.global.security.details.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Location", description = "위치 기록 및 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/locations")
public class LocationLogController {

    private final LocationLogService locationLogService;

    @Operation(summary = "현재 위치 ?분 주기 전송 (피보호자용)", description = "?분 주기로 피보호자의 현재 GPS 좌표와 배터리 상태를 서버로 전송합니다.")
    @PostMapping
    public ResponseEntity<LocationSaveResponse> saveLocationLog(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LocationSaveRequest request
    ) {
        LocationSaveInfo info = locationLogService.saveLocation(
                userDetails.getEmail(),
                LocationSaveCommand.from(request)
        );
        return ResponseEntity.ok(new LocationSaveResponse(info.isAlertTriggered()));
    }

    @Operation(summary = "파트너 실시간 위치 조회 (보호자용)", description = "피보호자의 가장 최근 위치를 조회합니다.")
    @GetMapping("/latest")
    public ResponseEntity<LatestLocationResponse> getLatestLocation() {

        return ResponseEntity.ok(null); // 임시 반환값
    }
}
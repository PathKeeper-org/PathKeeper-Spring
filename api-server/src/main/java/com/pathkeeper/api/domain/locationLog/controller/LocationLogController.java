package com.pathkeeper.api.domain.locationLog.controller;

import com.pathkeeper.api.domain.locationLog.dto.LatestLocationResponse;
import com.pathkeeper.api.domain.locationLog.dto.LocationSaveRequest;
import com.pathkeeper.api.domain.locationLog.dto.LocationSaveResponse;
import com.pathkeeper.api.domain.locationLog.service.LocationLogService;
import com.pathkeeper.api.global.security.details.CustomUserDetails;
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
@RequestMapping("/api/v1/locations")
public class LocationLogController {

    private final LocationLogService locationLogService;

    @Operation(summary = "현재 위치 ?분 주기 전송 (피보호자용)", description = "?분 주기로 피보호자의 현재 GPS 좌표와 배터리 상태를 서버로 전송합니다.")
    @PostMapping
    public ResponseEntity<LocationSaveResponse> saveLocationLog(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LocationSaveRequest request
    ) {
        return ResponseEntity.ok(locationLogService.saveLocation(userDetails.getEmail(), request));
    }

    @Operation(summary = "파트너 실시간 위치 조회 (보호자용)", description = "피보호자의 가장 최근 위치를 조회합니다.")
    @GetMapping("/latest")
    public ResponseEntity<LatestLocationResponse> getLatestLocation(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(locationLogService.getLatestLocation(userDetails.getEmail()));
    }
}
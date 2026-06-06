package com.pathkeeper.api.domain.location;

import com.pathkeeper.api.domain.location.dto.LocationRequest;
import com.pathkeeper.api.global.security.details.CustomUserDetails;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Location", description = "위치 데이터 수신 API")
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    @Operation(summary = "위치 데이터 전송",
            description = "피보호자의 현재 위치를 서버로 전송합니다. 비동기로 처리됩니다.")
    @SecurityRequirement(name = "jwtAuth")
    @Timed(value = "api.location.receive", description = "위치 수신 처리 시간")
    public ResponseEntity<Void> receive(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LocationRequest request) {

        locationService.receiveLocation(userDetails.getEmail(), request);

        return ResponseEntity.accepted().build();
    }
}

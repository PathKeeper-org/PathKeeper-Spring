package com.pathkeeper.backend.controller.alertHistory;

import com.pathkeeper.backend.controller.alertHistory.dto.AlertDeleteResponse;
import com.pathkeeper.backend.controller.alertHistory.dto.AlertHistoriesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Alert", description = "안심존 이탈, 배터리 부족 등 알림 발생 내역 API")
//@RestController
@RequestMapping("/api/alerts")
public class AlertHistoryController {

    @Operation(summary = "알림 내역 목록 조회", description = "나에게 발생했던 최근 알림(이탈, 배터리 경고 등) 내역을 최신순으로 페이징하여 가져옵니다.")
    @GetMapping
    public ResponseEntity<Page<AlertHistoriesResponse>> getAlertHistories(
            @Parameter(description = "페이징 정보 (page, size, sort). 기본값: 최신순 20개")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        return ResponseEntity.ok(null); // 임시 반환값
    }

    @Operation(summary = "특정 알림 삭제", description = "특정 알림 내역을 영구적으로 삭제합니다.")
    @DeleteMapping("/{alertId}") //
    public ResponseEntity<AlertDeleteResponse> deleteAlert(
            @Parameter(description = "삭제할 알림의 고유 ID", example = "1")
            @PathVariable Long alertId) {

        return ResponseEntity.ok(null); // 임시 반환값
    }

    @Operation(summary = "모든 알림 삭제", description = "모든 알림 내역을 한꺼번에 삭제합니다.")
    @DeleteMapping("/all")
    public ResponseEntity<AlertDeleteResponse> deleteAllAlerts() {

        return ResponseEntity.ok(null); // 임시 반환값
    }
}

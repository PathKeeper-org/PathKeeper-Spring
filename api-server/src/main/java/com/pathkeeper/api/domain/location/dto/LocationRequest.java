package com.pathkeeper.api.domain.location.dto;

import jakarta.validation.constraints.*;

/**
 * 위치 데이터 수신 요청 DTO.
 * 클라이언트가 보내는 JSON을 매핑하고 검증한다.
 */
public record LocationRequest(

        @NotNull(message = "위도는 필수입니다")
        @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다")
        @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다")
        Double lat,

        @NotNull(message = "경도는 필수입니다")
        @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다")
        @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다")
        Double lng,

        @Min(value = 0, message = "배터리 잔량은 0 이상이어야 합니다")
        @Max(value = 100, message = "배터리 잔량은 100 이하여야 합니다")
        Integer batteryLevel,    // nullable

        @NotNull(message = "측정 시각은 필수입니다")
        @Positive(message = "측정 시각은 양수여야 합니다")
        Long recordedAt          // epoch milli
) {
}
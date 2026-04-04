package com.pathkeeper.backend.domain.alertHistory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlertType {

    // 안심존 관련 알림
    ZONE_OUT("ZONE_OUT", "안심존 이탈"),
    ZONE_IN("ZONE_IN", "안심존 진입"),

    // 기기 상태 및 긴급 알림
    LOW_BATTERY("LOW_BATTERY", "배터리 경고"),
    CONNECTION_LOST("CONNECTION_LOST", "위치 수신 끊김"),
    SOS("SOS", "긴급 호출");

    private final String key;
    private final String title;
}

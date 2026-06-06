// UserState.java
package com.pathkeeper.processor.domain.state;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserState {

    private final String status;            // "INSIDE" or "OUTSIDE"
    private final int exitSuspicionCount;   // INSIDE 상태에서 누적된 OUTSIDE 카운트
    private final int returnSuspicionCount; // OUTSIDE 상태에서 누적된 INSIDE 카운트
    private final Double lastLat;
    private final Double lastLng;
    private final Long lastUpdate;          // epoch milli

    public static UserState initial(boolean isInside, double lat, double lng, long timestamp) {
        return UserState.builder()
                .status(isInside ? "INSIDE" : "OUTSIDE")
                .exitSuspicionCount(0)
                .returnSuspicionCount(0)
                .lastLat(lat)
                .lastLng(lng)
                .lastUpdate(timestamp)
                .build();
    }

    public boolean isInside() {
        return "INSIDE".equals(status);
    }
}
package com.pathkeeper.alert.domain.guardian;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GuardianInfo {
    private final Long guardianId;
    private final String name;
    private final String fcmToken;
}
package com.pathkeeper.backend.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    // Spring Security는 기본적으로 권한 코드에 "ROLE_" 접두사 요구
    GUARDIAN("ROLE_GUARDIAN", "보호자"),
    PROTEGE("ROLE_PROTEGE", "피보호자");

    private final String key;
    private final String title;
}

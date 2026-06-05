// common/src/main/java/com/pathkeeper/common/enums/Role.java
package com.pathkeeper.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    GUARDIAN("ROLE_GUARDIAN", "보호자"),
    PROTEGE("ROLE_PROTEGE", "피보호자");

    private final String key;
    private final String title;
}
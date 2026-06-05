package com.pathkeeper.api.global.security.dto;

public record AccessTokenInfo(
        String email,
        String role
) {
}

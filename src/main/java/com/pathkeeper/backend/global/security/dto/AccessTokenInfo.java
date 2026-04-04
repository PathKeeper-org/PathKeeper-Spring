package com.pathkeeper.backend.global.security.dto;

public record AccessTokenInfo(
        String email,
        String role
) {
}

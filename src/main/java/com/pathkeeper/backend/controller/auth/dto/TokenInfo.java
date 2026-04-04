package com.pathkeeper.backend.controller.auth.dto;

public record TokenInfo(
        String accessToken,
        String refreshToken
) {
}

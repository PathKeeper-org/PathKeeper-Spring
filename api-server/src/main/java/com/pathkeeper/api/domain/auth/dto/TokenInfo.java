package com.pathkeeper.api.domain.auth.dto;

public record TokenInfo(
        String accessToken,
        String refreshToken
) {
}

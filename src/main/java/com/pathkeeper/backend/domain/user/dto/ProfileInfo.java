package com.pathkeeper.backend.domain.user.dto;

import com.pathkeeper.backend.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

public record ProfileInfo(
        Long userId,
        String email,
        String name,
        String role,
        String partnerName
//        String inviteCode
) {
    public static ProfileInfo from(User user) {
        return new ProfileInfo(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().getTitle(),
                Optional.ofNullable(user.getPartner())
                        .map(User::getName)
                        .orElse(null)
//                user.getInviteCode()
        );
    }
}

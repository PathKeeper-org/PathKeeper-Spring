package com.pathkeeper.backend.global.security.refresh;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@AllArgsConstructor
@RedisHash(value = "refreshToken")
public class RefreshToken {
    @Id
    private String token;

    private String email;

    @TimeToLive
    private Long expiration;
}

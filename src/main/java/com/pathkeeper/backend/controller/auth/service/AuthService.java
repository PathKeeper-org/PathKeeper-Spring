package com.pathkeeper.backend.controller.auth.service;

import com.pathkeeper.backend.controller.auth.dto.LoginRequest;
import com.pathkeeper.backend.controller.auth.dto.SignupRequest;
import com.pathkeeper.backend.controller.auth.dto.TokenInfo;
import com.pathkeeper.backend.domain.user.User;
import com.pathkeeper.backend.domain.user.repository.UserRepository;
import com.pathkeeper.backend.global.exception.BusinessException;
import com.pathkeeper.backend.global.exception.ErrorCode;
import com.pathkeeper.backend.global.security.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    // Redis에 접근하기 위해 추가
    private final StringRedisTemplate redisTemplate;

    private static final String RT_PREFIX = "RT:";
    private static final String BLACKLIST_PREFIX = "BLACKLIST:";

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .email(request.email())
                .password(encodedPassword)
                .name(request.name())
                .role(request.role())
                .build();

        userRepository.save(user);
    }

    @Transactional
    public TokenInfo login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = tokenProvider.createAccessToken(user.getEmail(), user.getRole().name());

        String refreshToken = tokenProvider.createRefreshToken();
        // Redis에 Refresh Token 저장 (Key: 토큰 문자열, Value: 이메일)
        redisTemplate.opsForValue().set(
                RT_PREFIX + refreshToken,
                user.getEmail(),
                Duration.ofSeconds(tokenProvider.getRefreshTokenValiditySeconds())
                );

        return new TokenInfo(accessToken, refreshToken);
    }

    @Transactional
    public TokenInfo reissue(String refreshToken) {
        String redisKey = RT_PREFIX + refreshToken;
        // Redis에서 토큰 조회
        String email = redisTemplate.opsForValue().get(redisKey);

        if (email == null)
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);

        // Access Token을 새로 만들려면 email과 role이 필요하므로 User 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // [RTR] 사용된 기존 Refresh Token은 즉시 삭제
        redisTemplate.delete(redisKey);

        String newAccessToken = tokenProvider.createAccessToken(user.getEmail(), user.getRole().name());
        String newRefreshToken = tokenProvider.createRefreshToken();

        redisTemplate.opsForValue().set(
                RT_PREFIX + newRefreshToken,
                user.getEmail(),
                Duration.ofSeconds(tokenProvider.getRefreshTokenValiditySeconds())
        );

        return new TokenInfo(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        // Redis에서 해당 토큰을 찾아 삭제
        if (refreshToken != null)
            redisTemplate.delete(RT_PREFIX + refreshToken);

        if (accessToken != null) {
            long remainingExpiration = tokenProvider.getRemainingExpiration(accessToken);

            if (remainingExpiration > 0) {
                redisTemplate.opsForValue().set(
                        BLACKLIST_PREFIX + accessToken,
                        "logout",
                        Duration.ofMillis(remainingExpiration)
                );
            }
        }
    }
}
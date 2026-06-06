package com.pathkeeper.api.domain.auth.controller;

import com.pathkeeper.api.domain.auth.dto.*;
import com.pathkeeper.api.domain.auth.service.AuthService;
import com.pathkeeper.api.global.exception.BusinessException;
import com.pathkeeper.api.global.exception.ErrorCode;
import com.pathkeeper.api.global.security.jwt.TokenProvider;
import com.pathkeeper.api.global.security.cookie.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "회원가입, 로그인, 파트너 연결 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final TokenProvider tokenProvider;
    public static final String BEARER = "Bearer ";
    public static final String REFRESH = "refresh_token";

    @Operation(summary = "이메일 회원가입", description = "새로운 유저(보호자/피보호자)를 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 Access Token을 발급받습니다.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // Service에서 토큰 쌍(AT, RT)을 받아옴
        TokenInfo tokens = authService.login(request);

        // Refresh Token을 쿠키로 생성
        ResponseCookie cookie
                = CookieUtil.createCookie(
                        REFRESH,
                        tokens.refreshToken(),
                        tokenProvider.getRefreshTokenValiditySeconds()
        );

        // 응답 헤더에 쿠키를, 바디에는 Access Token만 담아서 반환
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponse(tokens.accessToken()));
    }

    @Operation(summary = "토큰 재발급", description = "쿠키의 Refresh Token을 이용해 새로운 토큰 쌍을 발급 받습니다.")
    @PostMapping("/api/v1/auth/reissue")
    public ResponseEntity<ReissueResponse> reissue(
            @Parameter(hidden = true)
            @CookieValue(value = REFRESH, required = false) String refreshToken) {

        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.MISSING_REFRESH_TOKEN);
        }

        // 1. 기존 RT 삭제 및 새로운 토큰 쌍 발급 (RTR)
        TokenInfo tokens = authService.reissue(refreshToken);

        // 2. 새로운 Refresh Token으로 쿠키 생성
        ResponseCookie cookie
                = CookieUtil.createCookie(REFRESH, tokens.refreshToken(), tokenProvider.getRefreshTokenValiditySeconds());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new ReissueResponse(tokens.accessToken()));
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 삭제하고 쿠키를 비웁니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            @Parameter(hidden = true)
            @CookieValue(value = REFRESH, required = false) String refreshToken) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        String accessToken = null;

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER))
            accessToken = bearerToken.substring(BEARER.length());

        // Redis에서 Refresh Token 삭제
        authService.logout(accessToken, refreshToken);

        ResponseCookie deleteCookie
                = CookieUtil.emptyCookie(REFRESH);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }
}
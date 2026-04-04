package com.pathkeeper.backend.global.security.jwt;

import com.pathkeeper.backend.global.security.dto.AccessTokenInfo;
import com.pathkeeper.backend.global.security.details.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    public static final String BEARER = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response
            , FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token) && tokenProvider.isAccessToken(token)) {
            Authentication authentication = getAuthentication(token);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER)) {
            return bearerToken.substring(BEARER.length());
        }
        return null;
    }

    private Authentication getAuthentication(String token) {
        AccessTokenInfo accessTokenInfo = tokenProvider.parseAccessToken(token);

        CustomUserDetails userDetails = new CustomUserDetails(
                accessTokenInfo.email(), accessTokenInfo.role());

        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }
}
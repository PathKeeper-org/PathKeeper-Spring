package com.pathkeeper.backend.global.security.jwt;

import com.pathkeeper.backend.global.security.dto.AccessTokenInfo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class TokenProvider {

    private final Key key;
    private final long accessTokenValidityTime;
    private final long refreshTokenValidityTime;

    private static final String ROLE_KEY = "role";
    private static final String TOKEN_TYPE_KEY = "type";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";

    // application.yml에서 jwt.secret 값을 가져와서 암호화 키(Key) 객체로 만듭니다.
    public TokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-validity-in-milliseconds}")
            long accessTokenValidityTime,
            @Value("${jwt.refresh-token-validity-in-milliseconds}")
            long refreshTokenValidityTime
    ) {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityTime = accessTokenValidityTime;
        this.refreshTokenValidityTime = refreshTokenValidityTime;
    }

    public String createAccessToken(String email, String role) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + accessTokenValidityTime);

        return Jwts.builder()
                .setSubject(email)
                .claim(ROLE_KEY, role)
                .claim(TOKEN_TYPE_KEY, ACCESS_TOKEN_TYPE)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    }

    public String createRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public Long getRefreshTokenValiditySeconds() {
        return this.refreshTokenValidityTime / 1000;
    }

    public boolean isAccessToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_KEY));

        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public AccessTokenInfo parseAccessToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String userId = claims.getSubject();
        String role = claims.get(ROLE_KEY, String.class);

        return new AccessTokenInfo(userId, role);
    }

    public long getRemainingExpiration(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();

            long now = System.currentTimeMillis();
            return Math.max(expiration.getTime() - now, 0);
        } catch (Exception e){
            return 0;
        }
    }
}

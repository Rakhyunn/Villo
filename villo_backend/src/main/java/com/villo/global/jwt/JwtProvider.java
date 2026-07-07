package com.villo.global.jwt;

import com.villo.global.exception.CustomException;
import com.villo.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtProvider {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-expiration}")
    private Long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    // Access Token 발급
    public String generateAccessToken(Long userId) {
        return generateToken(userId, accessExpiration);
    }

    // Refresh Token 발급
    public String generateRefreshToken(Long userId) {
        return generateToken(userId, refreshExpiration);
    }

    // 토큰에서 userId 추출
    public Long getUserId(String token) {
        return getPayload(token).get("userId", Long.class);
    }

    // 토큰 유효성 검증
    public boolean isValid(String token) {
        try {
            getPayload(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    // 시크릿 키 문자열로 SecretKey 객체 생성
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    private String generateToken(Long userId, long expiration) {
        Date now = new Date();
        return Jwts.builder()
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(getSecretKey())
                .compact();
    }

    // 토큰에서 payload(claims) 추출 — 유효하지 않으면 null 반환
    private Claims getPayload(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}

package com.villo.domain.auth.dto;

// 모바일 토큰 재발급 응답 — 새 토큰 쌍을 JSON body로 반환 (웹은 쿠키 사용, 이 응답 미사용)
public record TokenRefreshResponse(
        String accessToken,
        String refreshToken
) {}

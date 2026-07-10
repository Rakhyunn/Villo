package com.villo.domain.auth.dto;

// 모바일 토큰 재발급 요청 — 쿠키가 없는 모바일이 body로 refresh token을 전달
public record TokenRefreshRequest(
        String refreshToken
) {}

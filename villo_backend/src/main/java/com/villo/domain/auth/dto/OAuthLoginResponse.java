package com.villo.domain.auth.dto;

// 모바일 소셜 로그인 응답 — 우리 서비스의 JWT + 온보딩 필요 여부
public record OAuthLoginResponse(
        String accessToken,
        String refreshToken,
        boolean needsOnboarding // 닉네임 미설정 시 true → 앱이 온보딩 화면으로 분기
) {}

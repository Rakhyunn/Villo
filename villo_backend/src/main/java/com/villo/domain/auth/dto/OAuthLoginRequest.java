package com.villo.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

// 모바일 소셜 로그인 요청 — provider(카카오/네이버/구글)에서 발급받은 access token
public record OAuthLoginRequest(
        @NotBlank(message = "소셜 액세스 토큰을 입력해주세요.")
        String accessToken
) {}

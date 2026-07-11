package com.villo.domain.auth.controller;

import com.villo.domain.auth.dto.NicknameRequest;
import com.villo.domain.auth.dto.OAuthLoginRequest;
import com.villo.domain.auth.dto.OAuthLoginResponse;
import com.villo.domain.auth.dto.TokenRefreshRequest;
import com.villo.domain.auth.dto.TokenRefreshResponse;
import com.villo.domain.auth.service.AuthService;
import com.villo.global.exception.CustomException;
import com.villo.global.exception.ErrorCode;
import com.villo.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 · 온보딩 (닉네임 설정, 토큰 재발급, 로그아웃)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // 닉네임 중복 확인
    @Operation(summary = "닉네임 중복 확인", description = "사용 가능한 닉네임인지 검사")
    @GetMapping("/nickname/check")
    public ApiResponse<Void> checkNickname(
            @RequestParam String nickname
    ) {
        authService.checkNickname(nickname);
        return ApiResponse.ok("사용 가능한 닉네임입니다.");
    }

    // 닉네임 + 마을 이름 최초 설정
    @Operation(summary = "닉네임·마을 이름 최초 설정", description = "온보딩 — 닉네임과 마을 이름을 등록")
    @PostMapping("/nickname")
    public ApiResponse<Void> setNickname(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody NicknameRequest request
    ) {
        authService.setNickname(userId, request);
        return ApiResponse.ok("닉네임 설정이 완료되었습니다.");
    }

    // 모바일 소셜 로그인 (토큰 교환)
    @Operation(summary = "모바일 소셜 로그인", description = "provider(kakao/naver/google) access token을 검증하고 JWT를 JSON으로 발급 — 모바일 전용")
    @PostMapping("/oauth/{provider}")
    public ApiResponse<OAuthLoginResponse> oauthLogin(
            @PathVariable String provider,
            @Valid @RequestBody OAuthLoginRequest request
    ) {
        OAuthLoginResponse response = authService.oauthLogin(provider, request.accessToken());
        return ApiResponse.ok("로그인 되었습니다.", response);
    }

    // Access Token 재발급 (웹=쿠키 / 모바일=body)
    @Operation(summary = "Access Token 재발급", description = "웹은 Refresh Token 쿠키로 재발급(쿠키 세팅), 모바일은 body의 refreshToken으로 재발급(JSON 반환)")
    @PostMapping("/token/refresh")
    public ApiResponse<TokenRefreshResponse> reissueToken(
            @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
            @RequestBody(required = false) TokenRefreshRequest request
    ) {
        // 웹 경로 — 쿠키가 있으면 쿠키로 재발급 (토큰은 쿠키로만, body 미노출)
        if (cookieRefreshToken != null && !cookieRefreshToken.isBlank()) {
            authService.reissueToken(cookieRefreshToken);
            return ApiResponse.ok("토큰이 재발급되었습니다.");
        }

        // 모바일 경로 — body의 refreshToken으로 재발급 (새 토큰을 JSON으로 반환)
        if (request != null && request.refreshToken() != null && !request.refreshToken().isBlank()) {
            TokenRefreshResponse response = authService.reissueTokenForMobile(request.refreshToken());
            return ApiResponse.ok("토큰이 재발급되었습니다.", response);
        }

        throw new CustomException(ErrorCode.INVALID_TOKEN);
    }

    // 로그아웃
    @Operation(summary = "로그아웃", description = "Refresh Token 삭제 및 로그아웃")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal Long userId
    ) {
        authService.logout(userId);
        return ApiResponse.ok("로그아웃 되었습니다.");
    }
}

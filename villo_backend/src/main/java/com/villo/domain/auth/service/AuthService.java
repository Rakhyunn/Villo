package com.villo.domain.auth.service;

import com.villo.domain.auth.dto.NicknameRequest;
import com.villo.domain.auth.dto.OAuthLoginResponse;
import com.villo.domain.auth.dto.TokenRefreshResponse;
import com.villo.domain.auth.entity.User;
import com.villo.domain.auth.entity.type.Provider;
import com.villo.domain.auth.oauth2.OAuth2TokenVerifier;
import com.villo.domain.auth.oauth2.OAuth2UserInfo;
import com.villo.domain.auth.oauth2.OAuthUserRegistrar;
import com.villo.domain.auth.repository.UserRepository;
import com.villo.domain.village.entity.UserVillage;
import com.villo.domain.village.repository.UserVillageRepository;
import com.villo.global.exception.CustomException;
import com.villo.global.exception.ErrorCode;
import com.villo.global.jwt.JwtProvider;
import com.villo.global.rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserVillageRepository userVillageRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final OAuth2TokenVerifier oAuth2TokenVerifier;
    private final OAuthUserRegistrar oAuthUserRegistrar;
    private final Rq rq;

    // 닉네임 중복 확인
    public void checkNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    // 닉네임 + 마을 이름 최초 설정
    @Transactional
    public void setNickname(Long userId, NicknameRequest request) {
        // 닉네임 중복 확인
        if (userRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 닉네임 설정
        user.updateNickname(request.nickname());

        // 마을 이름 설정 (입력값 있으면 변경, 없으면 "나의 마을" 유지)
        if (request.villageName() != null && !request.villageName().isBlank()) {
            UserVillage village = userVillageRepository.findByUserId(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.VILLAGE_NOT_FOUND));
            village.updateVillageName(request.villageName());
        }
    }

    // 모바일 소셜 로그인 — provider 토큰 검증 → JWT 발급 (쿠키 아님, JSON 반환)
    @Transactional
    public OAuthLoginResponse oauthLogin(String providerName, String providerAccessToken) {
        // provider 파싱 (지원하지 않는 값이면 400)
        Provider provider;
        try {
            provider = Provider.valueOf(providerName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        // provider access token 검증 후 사용자 정보 추출
        OAuth2UserInfo userInfo = oAuth2TokenVerifier.verify(provider, providerAccessToken);

        // 유저 조회 또는 신규 가입 (웹과 동일 규칙)
        User user = oAuthUserRegistrar.findOrCreate(userInfo);
        Long userId = user.getId();

        // JWT 발급 + Refresh Token 저장 (Redis)
        String accessToken = jwtProvider.generateAccessToken(userId);
        String refreshToken = jwtProvider.generateRefreshToken(userId);
        refreshTokenService.save(userId, refreshToken);

        // 닉네임 미설정 → 온보딩 필요
        boolean needsOnboarding = user.getNickname() == null;

        return new OAuthLoginResponse(accessToken, refreshToken, needsOnboarding);
    }

    // Access Token 재발급 (웹) — 새 토큰을 쿠키로 세팅 (body에 노출하지 않음)
    public void reissueToken(String refreshToken) {
        TokenPair pair = rotateTokens(refreshToken);
        rq.setAccessTokenCookie(pair.accessToken());
        rq.setRefreshTokenCookie(pair.refreshToken());
    }

    // Access Token 재발급 (모바일) — 쿠키 없이 새 토큰 쌍을 JSON으로 반환
    public TokenRefreshResponse reissueTokenForMobile(String refreshToken) {
        TokenPair pair = rotateTokens(refreshToken);
        return new TokenRefreshResponse(pair.accessToken(), pair.refreshToken());
    }

    // 공통: Refresh Token 검증 + 새 토큰 발급 + Rotation
    private TokenPair rotateTokens(String refreshToken) {
        // Refresh Token 유효성 검증
        if (!jwtProvider.isValid(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtProvider.getUserId(refreshToken);

        // Redis에 저장된 토큰과 비교
        if (!refreshTokenService.isValid(userId, refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 새 토큰 발급
        String newAccessToken = jwtProvider.generateAccessToken(userId);
        String newRefreshToken = jwtProvider.generateRefreshToken(userId);

        // Refresh Token Rotation
        refreshTokenService.rotate(userId, newRefreshToken);

        return new TokenPair(newAccessToken, newRefreshToken);
    }

    // 내부 전용 — 새로 발급된 토큰 쌍
    private record TokenPair(String accessToken, String refreshToken) {}

    // 로그아웃
    public void logout(Long userId) {
        refreshTokenService.delete(userId);
        rq.deleteAuthCookies();
    }
}

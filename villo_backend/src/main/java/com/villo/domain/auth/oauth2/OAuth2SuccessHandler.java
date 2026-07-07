package com.villo.domain.auth.oauth2;

import com.villo.domain.auth.service.RefreshTokenService;
import com.villo.global.jwt.JwtProvider;
import com.villo.global.rq.Rq;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final Rq rq;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        Long userId = oAuth2User.getUser().getId();

        // JWT 발급
        String accessToken = jwtProvider.generateAccessToken(userId);
        String refreshToken = jwtProvider.generateRefreshToken(userId);

        // Redis에 Refresh Token 저장
        refreshTokenService.save(userId, refreshToken);

        // 쿠키 설정
        rq.setAccessTokenCookie(accessToken);
        rq.setRefreshTokenCookie(refreshToken);

        // 닉네임 미설정 → 닉네임 설정 페이지로
        // 닉네임 설정 완료 → 메인 페이지로
        String redirectUrl = oAuth2User.isNicknameSet()
                ? frontendBaseUrl + "/"
                : frontendBaseUrl + "/nickname";

        log.info("OAuth2 로그인 성공: userId={}, redirect={}", userId, redirectUrl);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
